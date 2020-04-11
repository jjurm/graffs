package uk.ac.cam.jm2186.graffs.db.model

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import uk.ac.cam.jm2186.graffs.graph.gen.GraphProducer
import uk.ac.cam.jm2186.graffs.graph.gen.RemovingEdgesGenerator
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import uk.ac.cam.jm2186.graffs.metric.Metric
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasure
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId

/* GraphGenerator */

fun CliktCommand.graphGenerator_name() =
    option("--name", help = "unique name of the graph generator", metavar = "NAME").required()

fun CliktCommand.graphGenerator_n() =
    option("-n", help = "Number of graphs to generate from each dataset").int().required().validate {
        require(it > 0) { "Value must be >=1" }
    }

fun CliktCommand.graphGenerator_method() =
    option(help = "Algorithm to generate graphs").choice(*GraphProducer.map.keys.toTypedArray())
        .default(RemovingEdgesGenerator.id)

fun CliktCommand.graphGenerator_params() =
    option(
        help = "Parameters to pass to the generator, delimited by comma",
        metavar = "FLOAT,..."
    ).double().split(delimiter = ",")
        .default(listOf())

fun CliktCommand.graphGenerator_seed() = option(help = "Seed for the generator").long()

/* Experiment */

fun CliktCommand.experiment_name(
    vararg names: String = arrayOf("--name"),
    help: String = "Unique name of the experiment"
) =
    option(*names, help = help, metavar = "NAME").required()

fun CliktCommand.experiment_datasets(): OptionWithValues<List<GraphDatasetId>?, List<GraphDatasetId>, GraphDatasetId> {
    return option(
        "--datasets", help = "Source dataset(s) to generate graphs from, delimited by comma",
        metavar = "DATASET,..."
    ).convert {
        GraphDataset(it, validate = true).id
    }.split(",")
}

fun CliktCommand.experiment_generator() =
    option("--generator", help = "Graph generator used to produce graph from source datasets", metavar = "NAME")

private fun CliktCommand.experiment_metrics0() =
    option(
        "--metrics", help = "Graph metrics whose robustness should be calculated, delimited by comma",
        metavar = Metric.map.keys.joinToString(separator = ",")
    ).split<String, MetricId>(",")

fun CliktCommand.experiment_metrics() = experiment_metrics0().validate { Metric.map.keys.containsAll(it) }
fun CliktCommand.experiment_metrics_required() =
    experiment_metrics0().required().validate { Metric.map.keys.containsAll(it) }

private fun CliktCommand.experiment_robustnessMeasures0() =
    option(
        "--robustnessMeasures", help = "Robustness measures that should be evaluated for each graph metric",
        metavar = RobustnessMeasure.map.keys.joinToString(separator = ",")
    ).split<String, RobustnessMeasureId>(",")

fun CliktCommand.experiment_robustnessMeasures() =
    experiment_robustnessMeasures0().validate { RobustnessMeasure.map.keys.containsAll(it) }

fun CliktCommand.experiment_robustnessMeasures_required() =
    experiment_robustnessMeasures0().required().validate { RobustnessMeasure.map.keys.containsAll(it) }

/* Plots */

fun CliktCommand.metric_name() =
    option(
        "--metric", help = "Graph metric to plot", metavar = Metric.map.keys.joinToString(separator = ",")
    ).required().validate { Metric.map.keys.contains(it) }
