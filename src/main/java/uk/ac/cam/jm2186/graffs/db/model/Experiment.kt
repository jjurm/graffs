package uk.ac.cam.jm2186.graffs.db.model

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.metric.Metric
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasure
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import javax.persistence.*

@Entity
class Experiment(
    name: String,

    @ManyToOne
    @LazyCollection(LazyCollectionOption.FALSE)
    val generator: GraphGenerator,
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    val metrics: MutableSet<MetricId> = mutableSetOf(),
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    val robustnessMeasures: MutableSet<RobustnessMeasureId> = mutableSetOf(),

    datasets: Collection<GraphDatasetId> = listOf()
) : NamedEntity(name) {

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn
    val graphCollections: MutableMap<GraphDatasetId, GraphCollection> = mutableMapOf()
    @OneToMany(mappedBy = "experiment", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val robustnessResults: MutableList<Robustness> = mutableListOf()

    val datasets get() = graphCollections.keys.toSet()

    init {
        graphCollections.putAll(
            datasets.map { it to GraphCollection() }
        )
    }
}


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

fun Experiment.printToConsole() {
    println(
        """- $name
            |  datasets: $datasets
            |  generator: $generator
            |  metrics: $metrics
            |  robustnessMeasures: $robustnessMeasures
        """.trimMargin()
    )
}
