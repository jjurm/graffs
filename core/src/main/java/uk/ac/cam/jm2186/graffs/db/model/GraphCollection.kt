package uk.ac.cam.jm2186.graffs.db.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.db.AbstractJpaPersistable
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import javax.persistence.*

@Entity
class GraphCollection(
    val dataset: GraphDatasetId
) : AbstractJpaPersistable<Long>() {

    @OneToMany(mappedBy = "graphcollection", cascade = [CascadeType.REMOVE], orphanRemoval = true /*, fetch = FetchType.LAZY*/)
    @LazyCollection(LazyCollectionOption.EXTRA) // Allow querying size without initialising
    val distortedGraphs: MutableList<DistortedGraph> = mutableListOf()

}
