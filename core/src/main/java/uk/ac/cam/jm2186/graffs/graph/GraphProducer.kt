package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.db.model.DistortedGraph

typealias GraphProducerId = String
typealias GraphProducerFactory = (seed: Long, params: List<Number>) -> GraphProducer

/**
 * An interface for objects capable of producing [Graph]s.
 */
interface GraphProducer {

    companion object {
        val map: Map<GraphProducerId, GraphProducerFactory> = listOf<GraphProducerInfo>(
            RemovingEdgesGenerator,
            LinearEdgeThresholdGraphProducer,
            RandomEdgeThresholdGraphProducer,
            IdentityGenerator
        ).map { info -> info.id to info.factory }.toMap()
    }

    val id: GraphProducerId

    fun produce(sourceGraph: Graph, n: Int): List<DistortedGraph>

}

interface GraphProducerInfo {
    val id: GraphProducerId
    val factory: GraphProducerFactory
}
