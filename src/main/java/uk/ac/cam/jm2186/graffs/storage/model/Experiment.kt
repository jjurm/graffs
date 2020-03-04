package uk.ac.cam.jm2186.graffs.storage.model

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.GraphDatasetId
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
    return option("--datasets", help = "Source dataset(s) to generate graphs from, delimited by comma").convert {
        GraphDataset(it, validate = true).id
    }.split(",")
}

fun CliktCommand.experiment_generator() =
    option("--generator", help = "Graph generator used to produce graph from source datasets", metavar = "NAME")

fun CliktCommand.experiment_metrics() =
    option("--metrics", help = "Graph metrics whose robustness should be calculated, delimited by comma")
        .split<String, MetricId>(",")

fun CliktCommand.experiment_robustnessMeasures() =
    option("--robustnessMeasures", help = "Robustness measures that should be evaluated for each graph metric")
        .split<String, RobustnessMeasureId>(",")

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
