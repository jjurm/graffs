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
    var generator: GraphGenerator,

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    var metrics: MutableSet<MetricId> = mutableSetOf(),

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    var robustnessMeasures: MutableSet<RobustnessMeasureId> = mutableSetOf(),

    datasets: Collection<GraphDatasetId> = listOf()
) : NamedEntity(name) {

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "experiment")
    @LazyCollection(LazyCollectionOption.FALSE)
    var graphCollections: MutableList<GraphCollection> = mutableListOf()

    @OneToMany(mappedBy = "experiment", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    val robustnessResults: MutableList<Robustness> = mutableListOf()

    val datasets get() = graphCollections.map { it.dataset }

    init {
        graphCollections.addAll(
            datasets.map { GraphCollection(it) }
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
