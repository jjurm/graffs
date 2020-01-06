package uk.ac.cam.jm2186.partii.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MissingParameter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.types.enum
import uk.ac.cam.jm2186.partii.metric.AverageDegreeMetric
import uk.ac.cam.jm2186.partii.storage.GraphDataset

class LoadDatasetsCommand : CliktCommand(
    name = "load-datasets",
    help = "Check if the given dataset can be loaded from the file system"
) {

    val datasets by argument("datasets", help = "datasets to load").enum(GraphDataset::id).multiple(required = true)

    override fun run() {
        datasets.forEach {dataset ->
            val graph = dataset.loadGraph()
            val averageDegree = AverageDegreeMetric().evaluate(graph)
            println("Dataset ${dataset.id} has ${graph.nodeCount} nodes with average degree ${"%.${2}f".format(averageDegree)}")
        }
    }

}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.nullableMultiple(): OptionWithValues<List<EachT>?, EachT, ValueT> {
    return transformAll {
        if (it.isEmpty()) null
        else it
    }
}
