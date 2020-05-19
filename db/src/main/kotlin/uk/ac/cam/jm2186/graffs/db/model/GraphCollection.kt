package uk.ac.cam.jm2186.graffs.db.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.db.AbstractJpaPersistable
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import javax.persistence.*

@Entity
class GraphCollection(
    val dataset: GraphDatasetId,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "experiment")
    val experiment: Experiment

) : AbstractJpaPersistable<Long>() {

    @OneToMany(mappedBy = "graphCollection", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    @OrderColumn
    @LazyCollection(LazyCollectionOption.EXTRA) // Allow querying size without initialising
    val perturbedGraphs: MutableList<PerturbedGraph> = mutableListOf()

}
