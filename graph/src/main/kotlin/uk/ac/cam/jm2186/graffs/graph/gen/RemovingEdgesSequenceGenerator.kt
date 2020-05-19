package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.db.model.PerturbedGraph
import uk.ac.cam.jm2186.graffs.graph.copy
import uk.ac.cam.jm2186.graffs.graph.hasWeights
import uk.ac.cam.jm2186.graffs.graph.subgraph
import kotlin.random.Random

/**
 * Generate a graph sequence by randomly removing edges from the previous graph. From a graph _G(N, E)_ in the sequence
 * produces _G(N, E')_ next, where _E' = [ e ∈ E | rand() < [deletionRate] ]_, i.e. each edge is removed with
 * probability [deletionRate]. The source graph is used as the first in the sequence.
 */
class RemovingEdgesSequenceGenerator(
    private val seed: Long,
    /** A number between 0 and 1, the probability to remove an edge. */
    private val deletionRate: Double,
    private val initialThreshold: Double? = null,
    private val coroutineScope: CoroutineScope
) : GraphProducer {

    companion object : GraphProducerInfo {
        override val id: GraphProducerId = "removing-edges"
        override val factory: GraphProducerFactory = { seed, params, coroutineScope ->
            RemovingEdgesSequenceGenerator(
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
            } else sourceGraph.copy()
        }

        fun perturbedAsync(seed: Long, graph: Deferred<Graph>) = coroutineScope.async {
            PerturbedGraph(seed, graph.await())
        }

        return generateSequence(seed to (baseGraph to perturbedAsync(0, baseGraph))) { (i, prev) ->
            val graphSeed = random.nextLong()
            val gen = coroutineScope.async {
                val prevGraph = prev.first.await()
                val id = "$baseId-$i"
                prevGraph.subgraph(id = id, edgeFilter = RandomElementRemoverFilter(deletionRate, seed))
            }
            (i + 1) to (gen to perturbedAsync(graphSeed, gen))
        }.map { it.second.second }.take(n).toList()
    }

}
