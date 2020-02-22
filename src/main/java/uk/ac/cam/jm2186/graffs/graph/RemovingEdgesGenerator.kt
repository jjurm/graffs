package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Edge
import org.graphstream.graph.Element
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.Graphs
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.util.Filter
import uk.ac.cam.jm2186.graffs.util.FilteredGraphReplay
import kotlin.random.Random

/**
 * Generate a graph by taking a [sourceGraph] and randomly removing its edges. From a given graph _G(N, E)_ produces
 * _G(N, E')_ where _E' = [ e ∈ E | rand() < [deletionRate] ]_, i.e. each edge is removed with probability
 * [deletionRate].
 */
class RemovingEdgesGenerator(
    private val sourceGraph: Graph,
    /** A number between 0 and 1, the probability to remove an edge. */
    private val deletionRate: Double,
    seed: Long
) : GraphProducer {

    companion object {
        const val ID: GraphProducerId = "removing-edges"
    }

    class Factory : GraphProducerFactory {
        override fun createGraphProducer(sourceGraph: Graph, seed: Long, params: List<Number>) =
            RemovingEdgesGenerator(
                sourceGraph,
                deletionRate = params[0] as Double,
                seed = seed
            )
    }

    private val random = Random(seed)
    private val id = sourceGraph.id + "-" + javaClass.name
    private var count = 0
    private val filteredReplay by lazy {
        FilteredGraphReplay(id, edgeFilter = RandomElementRemoverFilter())
    }

    override fun produce(): Graph {
        val graph = SingleGraph("$id-${count++}")
        filteredReplay.addSink(graph)
        return graph
    }

    override fun compute() {
        filteredReplay.replay(sourceGraph)
    }

    override fun produceComputed(): Graph {
        val cloned = Graphs.clone(sourceGraph)
        val filter = RandomElementRemoverFilter<Edge>()
        cloned.getEachEdge<Edge>().removeAll { !filter.isAvailable(it) }
        return cloned
    }

    private inner class RandomElementRemoverFilter<E : Element> : Filter<E> {
        private val decided = HashMap<E, Boolean>()
        override fun isAvailable(e: E): Boolean = decided.computeIfAbsent(e) { random.nextDouble() > deletionRate }
    }

}
