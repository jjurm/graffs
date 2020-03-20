package uk.ac.cam.jm2186.graffs.graph

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.Graphs
import uk.ac.cam.jm2186.graffs.db.model.DistortedGraph

object IdentityGenerator : GraphProducer, GraphProducerInfo {

    override val id: GraphProducerId = "identity"
    override val factory: GraphProducerFactory = { _, _ -> this }

    override fun produce(sourceGraph: Graph, n: Int, coroutineScope: CoroutineScope): List<Deferred<DistortedGraph>> {
        return (0 until n).map {
            coroutineScope.async {
                DistortedGraph(0L, Graphs.clone(sourceGraph))
            }
        }
    }

}
