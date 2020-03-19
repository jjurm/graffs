package uk.ac.cam.jm2186.graffs.db.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
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
