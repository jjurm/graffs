package uk.ac.cam.jm2186.graffs.db.model

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.GraphProducer
import uk.ac.cam.jm2186.graffs.graph.GraphProducerId
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType

@Entity
class GraphGenerator(
    name: String,

    val n: Int,
    val method: GraphProducerId,
    @ElementCollection(fetch = FetchType.EAGER)
    val params: List<Double>,
    val seed: Long
) : NamedEntity(name) {

    fun produceFromGraph(sourceGraph: Graph): List<DistortedGraph> {
        val generatorFactory = GraphProducer.map.getValue(method)
        return generatorFactory(seed, params)
            .produce(sourceGraph, n)
    }

    override fun toString(): String {
        return "$name(n=$n, method='$method', params=$params, seed=$seed)"
    }
}
