package uk.ac.cam.jm2186.graffs.db.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.db.NamedEntity
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import javax.persistence.*

/**
 * An evaluation of a _set of metrics_ using a set of _robustness measures_, generating graphs from a set of _datasets_
 * using a given _graph producer_.
 */
@Entity
class Experiment(
    name: String,

    @ManyToOne(fetch = FetchType.EAGER)
    var generator: GraphGenerator,

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    var metrics: List<MetricId> = listOf(),

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    var robustnessMeasures: List<RobustnessMeasureId> = listOf(),

    datasets: Collection<GraphDatasetId> = listOf()
) : NamedEntity(name) {

    /* GraphCollections are created from the [datasets] constructor param */
    @OneToMany(mappedBy = "experiment", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    var graphCollections: MutableList<GraphCollection> = datasets.map { GraphCollection(it, this) }.toMutableList()

    @OneToMany(mappedBy = "experiment", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    val robustnessResults: MutableList<Robustness> = mutableListOf()

    val datasets get() = graphCollections.map { it.dataset }
}

fun Experiment.printToConsole() {
    println(
        """- $name
            |  datasets: $datasets
            |  generator: $generator
            |  metrics: ${metrics.joinToString(",")}
            |  robustnessMeasures: ${robustnessMeasures.joinToString(",")}
        """.trimMargin()
    )
}
