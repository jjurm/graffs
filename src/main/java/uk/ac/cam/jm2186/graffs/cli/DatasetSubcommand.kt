package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.metric.AverageDegreeMetric
import uk.ac.cam.jm2186.graffs.storage.GraphDataset

class DatasetSubcommand : NoRunCliktCommand(
    name = "dataset",
    printHelpOnEmptyArgs = true,
    help = """
        Access available datasets
        
        Datasets are stored in the `${GraphDataset.DATASET_DIRECTORY}` folder
    """.trimIndent()
) {

    init {
        subcommands(
            ListDatasetsCommand(),
            LoadDatasetsCommand(),
            Visualise()
        )
    }

    companion object {
        fun getAvailableDatasetsWithMessages() = GraphDataset.getAvailableDatasets().also {
            when {
                it == null -> println("No `${GraphDataset.DATASET_DIRECTORY}` directory exists in the current path!")
                it.isEmpty() -> println("The `${GraphDataset.DATASET_DIRECTORY}` directory has no subdirectories.")
            }
        }
    }

    class ListDatasetsCommand : CliktCommand(
        name = "list",
        help = "List all datasets available in the `${GraphDataset.DATASET_DIRECTORY}` directory"
    ) {
        override fun run() {
            getAvailableDatasetsWithMessages()?.forEach { dataset ->
                println()
                println("- ${dataset.id}")
                dataset.loadInfo()?.let { println(it.trimEnd().prependIndent("  ")) }
            }
        }
    }

    class LoadDatasetsCommand : CliktCommand(
        name = "load",
        help = "Check if the given or all datasets can be loaded from the `${GraphDataset.DATASET_DIRECTORY}` directory"
    ) {

        val datasets by argument(
            "datasets",
            help = "Datasets to load. Leave empty to load all present datasets"
        ).convert { GraphDataset(it) }.multiple(required = false)

        override fun run() {
            val toLoad: List<GraphDataset>? = if (datasets.isNotEmpty()) datasets
            else getAvailableDatasetsWithMessages()

            toLoad?.forEach { dataset ->
                val graph = dataset.loadGraph()
                val averageDegree = AverageDegreeMetric().evaluate(graph)
                println(
                    "- ${dataset.id} has ${graph.nodeCount} nodes with average degree ${"%.${2}f".format(
                        averageDegree
                    )}"
                )
            }
        }

    }

    inner class Visualise : AbstractVisualiseSubcommand() {

        val dataset by argument(
            "<dataset>",
            help = "Dataset to visualise"
        ).convert { GraphDataset(it) }

        override fun getGraph(): Graph {
            try {
                return dataset.loadGraph()
            } catch (e: IllegalArgumentException) {
                throw BadParameterValue(e.message ?: "Could not load dataset", Visualise::dataset.name)
            }
        }
    }

}
