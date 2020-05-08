package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.graphstream.graph.Element
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.util.Filter
import uk.ac.cam.jm2186.graffs.db.model.PerturbedGraph
import uk.ac.cam.jm2186.graffs.graph.FilteredGraphReplay
import uk.ac.cam.jm2186.graffs.graph.hasWeights
import kotlin.random.Random

/**
 * Generate a graph by taking a source graph and randomly removing its edges. From a given graph _G(N, E)_ produces
 * _G(N, E')_ where _E' = [ e ∈ E | rand() < [deletionRate] ]_, i.e. each edge is removed with probability
 * [deletionRate].
 */
class RemovingEdgesGenerator(
    private val seed: Long,
    /** A number between 0 and 1, the probability to remove an edge. */
    private val deletionRate: Double,
    private val initialThreshold: Double?,
    private val coroutineScope: CoroutineScope
) : GraphProducer {

    companion object : GraphProducerInfo {
        override val id: GraphProducerId = "removing-edges"
        override val factory: GraphProducerFactory = { seed, params, coroutineScope ->
            RemovingEdgesGenerator(
                seed = seed,
                deletionRate = params[0].toDouble(),
                initialThreshold = params.getOrNull(1)?.toDouble(),
                coroutineScope = coroutineScope
            )
        }
    }

    override val id get() = Companion.id

    override fun produce(sourceGraph: Graph, n: Int): List<Deferred<PerturbedGraph>> {
        val baseId = sourceGraph.id + "-" + this::class.simpleName
        val random = Random(seed)
        val baseGraph = coroutineScope.async {
            if (initialThreshold != null && sourceGraph.hasWeights()) {
                sourceGraph.filterAtThreshold(initialThreshold)
            } else sourceGraph
        }

        return (0 until n).map { i ->
            coroutineScope.async {
                val g = baseGraph.await()
                val graphSeed = random.nextLong()
                val id = "$baseId-$i"
                produceSingle(g, graphSeed, id)
            }
        }
    }

    private fun produceSingle(sourceGraph: Graph, seed: Long, id: String): PerturbedGraph {
        val replay = FilteredGraphReplay(
            "$id-replay",
            edgeFilter = RandomElementRemoverFilter(seed)
        )
        val graph = SingleGraph(id)
        replay.addSink(graph)
        replay.replay(sourceGraph)
        replay.removeSink(graph)
        return PerturbedGraph(seed, graph)
    }

    private inner class RandomElementRemoverFilter<E : Element>(seed: Long) : Filter<E> {
        private val random = Random(seed)
        private val decided = HashMap<E, Boolean>()
        override fun isAvailable(e: E): Boolean = decided.computeIfAbsent(e) { random.nextDouble() > deletionRate }
    }

}
