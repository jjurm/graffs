package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.lang3.StringUtils.leftPad
import org.apache.commons.lang3.StringUtils.rightPad
import org.apache.commons.lang3.time.StopWatch
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.metric.Metric
import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasure
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.storage.*
import uk.ac.cam.jm2186.graffs.storage.model.*
import uk.ac.cam.jm2186.graffs.util.TimePerf

class ExperimentSubcommand : NoOpCliktCommand(
    name = "experiment",
    help = "Run experiments (evaluate metrics on generated graphs) and show results"
) {

    init {
        subcommands(
            ListSub(),
            CreateSub(),
            CloneSub(),
            RemoveSub(),
            RunSub(),
            PruneSub()
        )
    }

    class ListSub : AbstractHibernateCommand(
        name = "list",
        help = "List created experiments and their properties"
    ) {
        override fun run0() {
            hibernate.getAllEntities(Experiment::class.java).forEach {
                it.printToConsole()
            }
        }
    }

    class CreateSub : AbstractHibernateCommand(
        name = "create",
        help = "Create an experiment"
    ) {

        init {
            eagerOption("--sample", help = "Create a sample experiment (overriding all options)") {
                val generator = hibernate.getNamedEntity<GraphGenerator>("sampleGenerator")
                createExperiment(
                    Experiment(
                        name = "sampleExperiment",
                        generator = generator,
                        metrics = mutableSetOf("Degree", "PageRank", "Betweenness"),
                        robustnessMeasures = mutableSetOf("RankInstability"),
                        datasets = listOf("test")
                    )
                )
                throw PrintMessage("")
            }
        }

        private val name by experiment_name()
        private val datasets by experiment_datasets().required()
        private val generatorName by experiment_generator().required()
        private val metrics by experiment_metrics().required()
        private val robustnessMeasures by experiment_robustnessMeasures().required()

        override fun run0() {
            val generator = hibernate.getNamedEntity<GraphGenerator>(generatorName)
            createExperiment(
                Experiment(
                    name = name,
                    generator = generator,
                    metrics = metrics.toMutableSet(),
                    robustnessMeasures = robustnessMeasures.toMutableSet(),
                    datasets = datasets
                )
            )
        }

        private fun createExperiment(experiment: Experiment) {
            hibernate.mustNotExist<Experiment>(experiment.name)
            hibernate.beginTransaction()
            hibernate.save(experiment)
            hibernate.transaction.commit()

            experiment.printToConsole()
        }
    }

    class RemoveSub : CoroutineCommand(
        name = "remove",
        help = "Remove an experiment, all its generated graph and any computed results"
    ) {
        val name by experiment_name()
        override suspend fun run1() {
            val experiment = hibernate.getNamedEntity<Experiment>(name)
            hibernate.inTransaction { delete(experiment) }
        }
    }

    class RunSub : CoroutineCommand(
        name = "run",
        help = """Run an experiment
            |
            |```
            |Running an experiment has 3 phases:
            |1. Generate graphs
            |2. Evaluate metrics on graphs
            |3. Calculate robustness measures of graph metrics
            |```
            |
            |You can run the phases separately, or more/all at once.
        """.trimMargin()
    ) {

        private val phasesMap = mapOf(
            //"generate" to 1, "metrics" to 2, "robustness" to 3,
            "1" to 1, "2" to 2, "3" to 3,
            "all" to 3
        )
        private val phasesAll = listOf(::generate, ::metrics, ::robustness)

        private val experimentName by experiment_name()
        private val phase by argument(
            name = "phase",
            help = "Run only up to the specified phase [1|2|3|all]"
        ).choice(*phasesMap.keys.toTypedArray()).default("all")

        private val timer = TimePerf()

        override suspend fun run1() {
            val experiment: Experiment = hibernate.getNamedEntity<Experiment>(experimentName)

            val phaseIndex = phasesMap.getValue(phase)
            phasesAll.take(phaseIndex).forEach { phase ->
                phase(experiment)
            }
        }

        private suspend fun generate(experiment: Experiment) {
            timer.phase("Generate graphs")
            val hibernateMutex = Mutex()
            coroutineScope {
                experiment.graphCollections.forEach { (datasetId, graphCollection) ->
                    if (graphCollection.distortedGraphs.isEmpty()) {
                        launch {
                            val sourceGraph = GraphDataset(datasetId).loadGraph()
                            // generate graphs
                            val generated = experiment.generator.produceFromGraph(sourceGraph)
                            graphCollection.distortedGraphs.addAll(generated)
                            // store in database
                            hibernateMutex.withLock {
                                hibernate.inTransaction {
                                    save(graphCollection)
                                    generated.forEach { save(it) }
                                }
                            }
                        }
                    }
                }
            }
        }

        private suspend fun metrics(experiment: Experiment) {
            timer.phase("Evaluate metrics")
            val hibernateMutex = Mutex()
            val metrics = experiment.metrics.map {
                val info = Metric.map.getValue(it)
                val metric = info.factory()
                info to metric
            }

            coroutineScope {
                val graphJobs = mutableListOf<Deferred<Unit>>()

                experiment.graphCollections.forEach { (datasetId, graphCollection) ->
                    graphCollection.distortedGraphs.forEach { distortedGraph ->
                        val graph = distortedGraph.deserialize()
                        val graphMutex = Mutex()

                        val jobs = HashMap<MetricInfo, Deferred<Unit>>()
                        metrics.forEach { (metricInfo, metric) ->
                            val job = async(start = CoroutineStart.LAZY) {
                                // make sure that all dependencies are computed first
                                val dependencies = metricInfo.dependencies.map { jobs.getValue(it) }
                                dependencies.awaitAll()

                                // now compute the current metric
                                graphMutex.withLock {
                                    metric.evaluateAndLog(graph, datasetId, distortedGraph)
                                }
                            }
                            jobs[metricInfo] = job
                        }

                        // start and wait for all jobs
                        val graphJob = async(start = CoroutineStart.LAZY) {
                            jobs.values.awaitAll()
                            // store the result
                            distortedGraph.serialize(graph)
                            hibernateMutex.withLock {
                                hibernate.inTransaction { save(distortedGraph) }
                            }
                            Unit
                        }
                        graphJobs.add(graphJob)
                    }
                }

                val n = experiment.datasets.size * experiment.generator.n * experiment.metrics.size
                println("About to compute (at most) $n metric evaluations (${experiment.datasets.size} datasets * ${experiment.generator.n} generated * ${experiment.metrics.size} metrics)")
                graphJobs.awaitAll()
                println("Done.")

            }
        }

        private suspend fun Metric.evaluateAndLog(
            graph: Graph,
            datasetId: GraphDatasetId,
            distortedGraph: DistortedGraph
        ) {
            val stopWatch = StopWatch()
            stopWatch.start()
            val evaluated = evaluate(graph)
            stopWatch.stop()

            if (evaluated != null) {
                val sGraph = rightPad(datasetId, 16)
                val sHash = leftPad(distortedGraph.getShortHash(), 4)
                val sMetric = rightPad(id, 20)
                val sResult = leftPad(evaluated.toString(), 6)
                val sTime = "${stopWatch.time / 1000}s"
                println("- $sGraph (seed $sHash) -> $sMetric = $sResult  ($sTime)")
            }
        }

        private suspend fun robustness(experiment: Experiment) {

        }
    }

    class CloneSub : CoroutineCommand(
        name = "clone",
        help = "Create a new experiment using parameters of existing experiment"
    ) {

        val from by experiment_name("--from", help = "Template experiment to copy properties from")
        val name by experiment_name()

        private val datasets by experiment_datasets()
        private val generatorName by experiment_generator()
        private val metrics by experiment_metrics()
        private val robustnessMeasures by experiment_robustnessMeasures()

        override suspend fun run1() {
            val from = hibernate.getNamedEntity<Experiment>(from)
            val generator = when (val name = generatorName) {
                null -> from.generator
                else -> hibernate.getNamedEntity<GraphGenerator>(name)
            }
            createExperiment(
                Experiment(
                    name = name,
                    generator = generator,
                    metrics = metrics?.toMutableSet() ?: from.metrics.toMutableSet(),
                    robustnessMeasures = robustnessMeasures?.toMutableSet() ?: from.robustnessMeasures.toMutableSet(),
                    datasets = datasets ?: from.datasets
                )
            )
        }

        private suspend fun createExperiment(experiment: Experiment) {
            hibernate.inTransaction { save(experiment) }
            experiment.printToConsole()
        }
    }

    class PruneSub : CoroutineCommand(
        name = "prune",
        help = "Remove all generated graphs of an experiment"
    ) {
        val name by experiment_name()
        override suspend fun run1() {
            val experiment = hibernate.getNamedEntity<Experiment>(name)
            hibernate.inTransaction {
                experiment.graphCollections.values.forEach {
                    it.distortedGraphs.clear()
                    save(it)
                }
            }
        }
    }

}
