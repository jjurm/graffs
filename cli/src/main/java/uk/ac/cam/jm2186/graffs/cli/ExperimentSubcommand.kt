package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.lang3.StringUtils.leftPad
import org.apache.commons.lang3.StringUtils.rightPad
import org.apache.commons.lang3.time.StopWatch
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import uk.ac.cam.jm2186.graffs.metric.Metric
import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.robustness.GraphCollectionMetadata
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasure
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.db.*
import uk.ac.cam.jm2186.graffs.db.model.*
import uk.ac.cam.jm2186.graffs.metric.MetricResult
import uk.ac.cam.jm2186.graffs.metric.evaluateMetricsAsync
import uk.ac.cam.jm2186.graffs.util.TimePerf
import java.util.concurrent.atomic.AtomicInteger

class ExperimentSubcommand : NoOpCliktCommand(
    name = "experiment",
    help = "Manage experiments (evaluating metrics on generated graphs)"
) {

    init {
        subcommands(
            ListSub(),
            CreateSub(),
            ChangeSub(),
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
                        metrics = listOf("Degree", "PageRank", "Betweenness"),
                        robustnessMeasures = listOf("RankIdentifiability", "RankInstability"),
                        datasets = listOf("test")
                    )
                )
                throw PrintMessage("")
            }
        }

        private val name by experiment_name()
        private val datasets by experiment_datasets().required()
        private val generatorName by experiment_generator().required()
        private val metrics by experiment_metrics_required()
        private val robustnessMeasures by experiment_robustnessMeasures_required()

        override fun run0() {
            val generator = hibernate.getNamedEntity<GraphGenerator>(generatorName)
            createExperiment(
                Experiment(
                    name = name,
                    generator = generator,
                    metrics = metrics,
                    robustnessMeasures = robustnessMeasures,
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

    class ChangeSub : CoroutineCommand(
        name = "change",
        help = """Change an existing experiment
            |
            |Removing a graph metric from the list will not remove computed values of that metric on generated graphs.
            |Removing a dataset from an experiment will cause all computations on that dataset to be deleted too.
            |Any change will delete calculated robustness results.
        """.trimMargin()
    ) {

        val name by experiment_name()

        private val datasets by experiment_datasets()
        private val generatorName by experiment_generator()
        private val metrics by experiment_metrics()
        private val robustnessMeasures by experiment_robustnessMeasures()

        override suspend fun run1() {
            val experiment = hibernate.getNamedEntity<Experiment>(name)

            datasets?.let { newDatasets ->
                val newGraphCollections = newDatasets.map { dataset ->
                    experiment.graphCollections.firstOrNull { dataset == it.dataset }
                        ?: GraphCollection(dataset, experiment)
                }
                experiment.graphCollections = newGraphCollections.toMutableList()
            }
            generatorName?.let {
                experiment.generator = hibernate.getNamedEntity<GraphGenerator>(it)
                experiment.graphCollections.forEach { it.perturbedGraphs.clear() }
            }
            metrics?.let { experiment.metrics = it }
            robustnessMeasures?.let { experiment.robustnessMeasures = it }

            hibernate.inTransaction {
                experiment.robustnessResults.clear()
                save(experiment)
            }
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

        private val phasesArgMapping = mapOf(
            //"generate" to 1, "metrics" to 2, "robustness" to 3,
            "1" to listOf(0), "2" to listOf(1), "3" to listOf(2),
            "all" to listOf(0, 1, 2)
        )
        private val phasesAvailable = listOf(::generate, ::metrics, ::robustness)

        private val experimentName by experiment_name()
        private val phases by argument(
            name = "phase",
            help = "Run only up to the specified phase [1|2|3|all]"
        ).choice(*phasesArgMapping.keys.toTypedArray()).multiple(required = false)

        private val timer = TimePerf()

        override suspend fun run1() {
            val experiment: Experiment = hibernate.getNamedEntity<Experiment>(experimentName)
            experiment.printToConsole()
            println()

            (if (phases.isEmpty()) listOf("all") else phases)
                .flatMap {
                    phasesArgMapping.getValue(it).map { phasesAvailable[it] }
                }.forEach { phase ->
                    phase(experiment)
                }
            println("Done.")
            timer.finish()
            timer.printToConsole()
        }

        private suspend fun generate(experiment: Experiment) {
            print(timer.phase("Generate graphs"))
            val hibernateMutex = Mutex()
            coroutineScope {
                experiment.graphCollections.forEach { graphCollection ->
                    if (graphCollection.perturbedGraphs.isEmpty()) {
                        launch {
                            val sourceGraph = GraphDataset(graphCollection.dataset).loadGraph()
                            // Generate graphs
                            val generated = experiment.generator
                                .produceFromGraph(sourceGraph, this)

                            val savedGraphs = generated.map {
                                // Store each graph in the database as soon as it is generated and serialized
                                async {
                                    val graph = it.await()
                                    graph.graphCollection = graphCollection
                                    hibernateMutex.withLock {
                                        hibernate.inTransaction {
                                            save(graph)
                                        }
                                    }
                                    print(".")
                                    graph
                                }
                            }
                            graphCollection.perturbedGraphs.addAll(savedGraphs.awaitAll())
                            hibernateMutex.withLock {
                                hibernate.inTransaction {
                                    save(graphCollection)
                                }
                            }
                        }
                    }
                }
            }
            println()
        }

        private suspend fun metrics(experiment: Experiment) {
            val nEvaluations = experiment.datasets.size * experiment.generator.n * experiment.metrics.size
            val nEvaluated = AtomicInteger(0)

            suspend fun evaluateAndLog(
                metricEvaluation: suspend () -> MetricResult?,
                metric: MetricInfo,
                datasetId: GraphDatasetId,
                perturbedGraph: PerturbedGraph
            ) {
                val stopWatch = StopWatch()
                stopWatch.start()
                val evaluated = metricEvaluation()
                stopWatch.stop()

                if (evaluated != null) {
                    val sProgress = leftPad(nEvaluated.incrementAndGet().toString(), nEvaluations.toString().length)
                    val sDataset = rightPad(datasetId, 16)
                    val sHash = leftPad(perturbedGraph.getShortHash(), 4)
                    val sMetric = rightPad(metric.id, 20)
                    val sResult = leftPad(evaluated.toString(), 6)
                    val sTime = "${stopWatch.time / 1000}s"
                    println("[$sProgress/$nEvaluations] $sDataset (seed $sHash) -> $sMetric = $sResult  ($sTime)")
                }
            }

            println("Evaluate metrics")
            timer.phase("Evaluate metrics - prepare")
            val hibernateMutex = Mutex()
            val metrics = experiment.metrics.map { Metric.map.getValue(it) }

            coroutineScope {
                val graphJobs = mutableListOf<Deferred<Unit>>()

                experiment.graphCollections.forEach { graphCollection ->
                    graphCollection.perturbedGraphs.forEach { distortedGraph ->

                        val graphJob = evaluateMetricsAsync(metrics,
                            getGraph = { distortedGraph.graph },
                            evaluateAndLog = { metricEvaluation, metric ->
                                evaluateAndLog(
                                    metricEvaluation,
                                    metric,
                                    graphCollection.dataset,
                                    distortedGraph
                                )
                            },
                            storeResults = { graph ->
                                distortedGraph.graph = graph
                                hibernateMutex.withLock {
                                    hibernate.inTransaction { save(distortedGraph) }
                                }
                            }
                        )

                        graphJobs.add(graphJob)
                    }
                }

                println("Running $nEvaluations metric evaluations (at most ${experiment.datasets.size} datasets * ${experiment.generator.n} generated * ${experiment.metrics.size} metrics)")
                timer.phase("Evaluate metrics - run")
                graphJobs.awaitAll()
            }
        }

        private suspend fun robustness(experiment: Experiment) {
            timer.phase("Robustness - prepare")
            val hibernateMutex = Mutex()
            val measures = experiment.robustnessMeasures.map {
                it to RobustnessMeasure.map.getValue(it)()
            }
            val metrics = experiment.metrics.map {
                Metric.map.getValue(it)
            }.filter { it.isNodeMetric }

            coroutineScope {
                val jobs = experiment.graphCollections.flatMap { graphCollection ->
                    metrics.flatMap { metric ->
                        val metadata = GraphCollectionMetadata(graphCollection, metric, this)
                        measures.map { (measureId, measure) ->
                            async(start = CoroutineStart.LAZY) {
                                val result = measure.evaluateAndLog(
                                    metric, graphCollection, metadata, graphCollection.dataset, measureId
                                )
                                val robustness =
                                    Robustness(experiment, graphCollection.dataset, metric.id, measureId, result)

                                hibernateMutex.withLock {
                                    hibernate.inTransaction { saveOrUpdate(robustness) }
                                }
                            }
                        }
                    }
                }

                val n = experiment.datasets.size * experiment.metrics.size * experiment.robustnessMeasures.size
                println("Computing (at most) $n robustness values (${experiment.datasets.size} datasets * ${experiment.metrics.size} metrics * ${experiment.robustnessMeasures.size} robustness measures)")
                timer.phase("Robustness - compute")
                jobs.awaitAll()
            }
        }

        private suspend fun RobustnessMeasure.evaluateAndLog(
            metric: MetricInfo,
            graphCollection: GraphCollection,
            metadata: GraphCollectionMetadata,
            datasetId: GraphDatasetId,
            measureId: RobustnessMeasureId
        ): Double {
            val result = evaluate(metric, graphCollection, metadata)

            val sDataset = rightPad("`$datasetId`", 18)
            val sMetric = rightPad(metric.id, 20)
            val sMeasure = rightPad(measureId, 20)
            val sResult = leftPad("%.7f".format(result), 10)
            println("- Dataset $sDataset -> metric $sMetric -> robustnessMeasure $sMeasure = $sResult")
            return result
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
                    metrics = metrics ?: from.metrics.toList(),
                    robustnessMeasures = robustnessMeasures ?: from.robustnessMeasures.toList(),
                    datasets = datasets ?: from.datasets.toList()
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
        help = "Remove generated graphs and calculated robustness values of an experiment"
    ) {
        val name by experiment_name()
        override suspend fun run1() {
            val experiment = hibernate.getNamedEntity<Experiment>(name)
            hibernate.inTransaction {
                experiment.graphCollections.forEach {
                    it.perturbedGraphs.clear()
                    save(it)
                }
                experiment.robustnessResults.clear()
                save(experiment)
            }
            println("Done.")
        }
    }

}
