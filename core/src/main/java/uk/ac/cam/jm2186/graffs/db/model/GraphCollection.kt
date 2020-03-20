package uk.ac.cam.jm2186.graffs.db.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.db.AbstractJpaPersistable
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

@Entity
class GraphCollection : AbstractJpaPersistable<Long>() {

    @OneToMany(cascade = [CascadeType.REMOVE], orphanRemoval = true /*, fetch = FetchType.LAZY*/)
    @LazyCollection(LazyCollectionOption.EXTRA) // Allow querying size without initialising
    @JoinColumn(name = "graphcollection")
    val distortedGraphs: MutableList<DistortedGraph> = mutableListOf()

}
