package uk.ac.cam.jm2186.graffs.db.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.db.AbstractJpaPersistable
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

@Entity
class GraphCollection(
    val dataset: GraphDatasetId
) : AbstractJpaPersistable<Long>() {

    @OneToMany(cascade = [CascadeType.REMOVE], orphanRemoval = true /*, fetch = FetchType.LAZY*/)
    @LazyCollection(LazyCollectionOption.EXTRA) // Allow querying size without initialising
    @JoinColumn(name = "graphcollection")
    val distortedGraphs: MutableList<DistortedGraph> = mutableListOf()

}
