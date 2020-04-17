package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.db.model.PerturbedGraph
import uk.ac.cam.jm2186.graffs.graph.copy

object IdentityGenerator : GraphProducer,
    GraphProducerInfo {

    override val id: GraphProducerId = "identity"
    override val factory: GraphProducerFactory = { _, _ -> this }

    override fun produce(sourceGraph: Graph, n: Int, coroutineScope: CoroutineScope): List<Deferred<PerturbedGraph>> {
        return (0 until n).map {
            coroutineScope.async {
                PerturbedGraph(0L, sourceGraph.copy())
            }
        }
    }

}
