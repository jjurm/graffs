package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.db.model.PerturbedGraph
import uk.ac.cam.jm2186.graffs.graph.filterAtThreshold
import uk.ac.cam.jm2186.graffs.graph.hasWeights
import uk.ac.cam.jm2186.graffs.graph.subgraph
import kotlin.random.Random

/**
 * Generate a graph by taking a source graph and randomly removing its edges. From a given graph _G(N, E)_ produces
 * _G(N, E')_ where _E' = [ e ∈ E | rand() < [deletionRate] ]_, i.e. each edge is removed with probability
 * [deletionRate].
 */
class RemovingEdgesFlatGenerator(
    private val seed: Long,
    /** A number between 0 and 1, the probability to remove an edge. */
    private val deletionRate: Double,
    private val initialThreshold: Double? = null,
    private val coroutineScope: CoroutineScope
) : GraphProducer {

    companion object : GraphProducerInfo {
        override val id: GraphProducerId = "removing-edges-flat"
        override val factory: GraphProducerFactory = { seed, params, coroutineScope ->
            RemovingEdgesFlatGenerator(
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
                val gen = g.subgraph(id = id, edgeFilter = RandomElementRemoverFilter(deletionRate, graphSeed))
                PerturbedGraph(i, gen)
            }
        }
    }
}
