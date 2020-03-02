package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.coroutines.*
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.getAllEntities
import uk.ac.cam.jm2186.graffs.storage.inTransaction
import uk.ac.cam.jm2186.graffs.storage.model.*
import kotlin.coroutines.coroutineContext

class ExperimentSubcommand : NoRunCliktCommand(
    name = "experiment",
    help = "Run experiments (evaluate metrics on generated graphs) and show results"
) {

    init {
        subcommands(
            ListSub(),
            CreateSub(),
            RemoveSub(),
            RunSub()
        )
    }

    class ListSub : AbstractHibernateCommand(
        name = "list",
        help = "List created experiments and their properties"
    ) {
        override fun run0() {
            hibernate.getAllEntities(Experiment::class.java).forEach {
                println(
                    """- ${it.name}
                        |  datasets: ${it.datasets}
                        |  generator: ${it.generator.name}
                        |  metrics: ${it.metrics}
                        |  robustnessMeasures: ${it.robustnessMeasures}}
                    """.trimMargin()
                )
            }
        }
    }

    class CreateSub : CoroutineCommand(
        name = "create",
        help = "Create an experiment"
    ) {
        private val name by experiment_name()
        private val datasets by experiment_datasets()
        private val generatorName by experiment_generator()
        private val metrics by experiment_metrics()
        private val robustnessMeasures by experiment_robustnessMeasures()

        override suspend fun run1() {
            val generator = hibernate.get(GraphGenerator::class.java, generatorName)
            val experiment = Experiment(
                name = name,
                generator = generator,
                metrics = metrics.toMutableList(),
                robustnessMeasures = robustnessMeasures.toMutableList()
            )
            experiment.graphCollections.putAll(
                datasets.map { it to GraphCollection() }
            )
            hibernate.inTransaction { save(experiment) }
        }
    }

    class RemoveSub : CoroutineCommand(
        name = "remove",
        help = "Remove an experiment, all its generated graph and any computed results"
    ) {
        val name by experiment_name()
        override suspend fun run1() {
            val experiment = hibernate.get(Experiment::class.java, name)
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
            "generate" to 1,
            "metrics" to 2,
            "robustness" to 3,
            "*" to 3
        )
        private val phasesAll = listOf(::generate, ::metrics, ::robustness)

        private val experimentName by experiment_name()
        private val phaseIndex by argument(
            name = "phase",
            help = "Run all up to the specified phase (* for all phases)"
        ).choice(phasesMap)

        override suspend fun run1() {
            val experiment = hibernate.get(Experiment::class.java, experimentName)

            phasesAll.take(phaseIndex).forEach { phase ->
                phase(experiment)
            }
        }

        suspend fun generate(experiment: Experiment) = hibernate.inTransaction {
            coroutineScope {
                experiment.graphCollections.forEach { datasetId, graphCollection ->
                    if (graphCollection.distortedGraphs.isEmpty()) {
                        launch {
                            val sourceGraph = GraphDataset(datasetId).loadGraph()
                            val generated = experiment.generator.produceFromGraph(sourceGraph)
                            graphCollection.distortedGraphs.addAll(
                                generated.map { DistortedGraph(it) }
                            )
                        }
                    }
                }
            }
            save(experiment)
        }

        suspend fun metrics(experiment: Experiment) {

        }

        suspend fun robustness(experiment: Experiment) {

        }
    }

}
