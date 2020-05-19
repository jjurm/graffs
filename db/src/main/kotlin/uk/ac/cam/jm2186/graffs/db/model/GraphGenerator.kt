package uk.ac.cam.jm2186.graffs.db.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.ac.cam.jm2186.graffs.db.NamedEntity
import uk.ac.cam.jm2186.graffs.graph.gen.GraphProducerId
import javax.persistence.*

@Entity
class GraphGenerator(
    name: String,

    val n: Int,
    val method: GraphProducerId,

    @ElementCollection
    @CollectionTable(name = "graphgenerator_params")
    @OrderColumn
    @LazyCollection(LazyCollectionOption.FALSE)
    val params: List<Double>,

    val seed: Long
) : NamedEntity(name) {

    override fun toString(): String {
        return "$name(n=$n, method='$method', params=$params, seed=$seed)"
    }
}
