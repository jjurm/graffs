package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.db.model.PerturbedGraph

typealias GraphProducerId = String
typealias GraphProducerFactory = (seed: Long, params: List<Number>, coroutineScope: CoroutineScope) -> GraphProducer

/**
 * An interface for objects capable of producing [Graph]s.
 */
interface GraphProducer {

    companion object {
        val map: Map<GraphProducerId, GraphProducerFactory> = listOf<GraphProducerInfo>(
            RemovingEdgesSequenceGenerator,
            RemovingEdgesFlatGenerator,
            LinearEdgeThresholdGraphProducer,
            RandomEdgeThresholdGraphProducer
        ).map { info -> info.id to info.factory }.toMap()
    }

    val id: GraphProducerId

    fun produce(sourceGraph: Graph, n: Int): List<Deferred<PerturbedGraph>>

}

interface GraphProducerInfo {
    val id: GraphProducerId
    val factory: GraphProducerFactory
}
