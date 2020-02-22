package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Graph
import org.graphstream.stream.Source

typealias GraphProducerId = String

/**
 * An interface for objects capable of producing [Graph]s.
 */
interface GraphProducer {

    companion object {
        val map: Map<GraphProducerId, Class<out GraphProducerFactory>> = mapOf(
            RemovingEdgesGenerator.ID to RemovingEdgesGenerator.Factory::class.java,
            IdentityGenerator.ID to IdentityGenerator.Factory::class.java
        )
    }

    /**
     * Produces an empty graph that will further be the [Source] of graph events.
     */
    fun produce(): Graph

    /**
     * Run the algorithm on the graphs created by preceding calls to [produce].
     * [compute] should only be called once.
     */
    fun compute()

    /**
     * A combined function of a single [produce] call followed by [compute], that returns the single produced call. This
     * may do these to operations together more efficiently. Note that using this method there is no way to subscribe
     * for graph events before the graph is being built.
     */
    fun produceComputed(): Graph {
        val graph = produce()
        compute()
        return graph
    }

}
