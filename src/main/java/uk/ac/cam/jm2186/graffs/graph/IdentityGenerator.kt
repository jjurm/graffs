package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.Graphs
import uk.ac.cam.jm2186.graffs.storage.model.DistortedGraph

object IdentityGenerator : GraphProducer, GraphProducerInfo {

    override val id: GraphProducerId = "identity"
    override val factory: GraphProducerFactory = { _, _ -> this }

    override fun produce(sourceGraph: Graph, n: Int): List<DistortedGraph> {
        return (0 until n).map { DistortedGraph(0L, Graphs.clone(sourceGraph)) }
    }

}
