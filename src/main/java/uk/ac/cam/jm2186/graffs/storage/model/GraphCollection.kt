package uk.ac.cam.jm2186.graffs.storage.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.storage.AbstractJpaPersistable
import javax.persistence.ElementCollection
import javax.persistence.Entity

@Entity
class GraphCollection : AbstractJpaPersistable<Long>() {

    //@OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true /*, fetch = FetchType.LAZY*/)
    @ElementCollection
    @LazyCollection(LazyCollectionOption.EXTRA) // Allow querying size without initialising
    val distortedGraphs: MutableList<DistortedGraph> = mutableListOf()

}
