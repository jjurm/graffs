package uk.ac.cam.jm2186.graffs.storage.model

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.choice
import uk.ac.cam.jm2186.graffs.metric.Metric
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.GraphDatasetId
import java.io.Serializable
import javax.persistence.*

@Entity
class Experiment(
    @Id
    val name: String,

    @ElementCollection(fetch = FetchType.EAGER)
    val datasets: MutableList<GraphDatasetId> = mutableListOf(),
    @ManyToOne(fetch = FetchType.EAGER)
    var generator: GraphGenerator?,
    @ElementCollection(fetch = FetchType.EAGER)
    val metrics: MutableList<MetricId> = mutableListOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    val robustnessMeasures: MutableList<RobustnessMeasureId> = mutableListOf()
) : Serializable


fun CliktCommand.experiment_name() =
    option("--name", help = "Unique name of the experiment", metavar = "NAME").required()

fun CliktCommand.experiment_datasets() =
    option("--datasets", help = "Source dataset(s) to generate graphs from, delimited by comma").convert {
        GraphDataset(it, validate = true).id
    }.split(",").required()

fun CliktCommand.experiment_generator() =
    option("--generator", help = "Graph generator used to produce graph from source datasets", metavar = "NAME")
        .required()

fun CliktCommand.experiment_metrics() =
    option("--metrics", help = "Graph metrics whose robustness should be calculated, delimited by comma")
        .split<String, MetricId>(",").required()

fun CliktCommand.experiment_robustnessMeasures() = option("--robustnessMeasures", help = "Robustness measures that should be evaluated for each graph metric")
    .split<String, RobustnessMeasureId>(",").required()
