package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.util.Filter
import uk.ac.cam.jm2186.graffs.db.model.PerturbedGraph
import uk.ac.cam.jm2186.graffs.graph.ATTRIBUTE_NAME_EDGE_WEIGHT
import uk.ac.cam.jm2186.graffs.graph.subgraph

abstract class AbstractEdgeThresholdGraphProducer(
    protected val getThresholds: (n: Int) -> List<Double>,
    protected val coroutineScope: CoroutineScope
) : GraphProducer {

    companion object {
        const val ATTRIBUTE_EDGE_THRESHOLD = "edgeThreshold"
    }

    override fun produce(
        sourceGraph: Graph,
        n: Int
    ): List<Deferred<PerturbedGraph>> {
        val thresholds = getThresholds(n)
        val baseId = sourceGraph.id + "-" + this::class.simpleName

        return thresholds.mapIndexed { i, threshold ->
            coroutineScope.async {
                val graph = sourceGraph.filterAtThreshold(threshold, baseId, i)
                PerturbedGraph(i.toLong(), graph)
            }
        }
    }

    internal class EdgeThresholdFilter(val threshold: Double) : Filter<Edge> {
        override fun isAvailable(e: Edge): Boolean =
            e.getAttribute<Double>(ATTRIBUTE_NAME_EDGE_WEIGHT)!! > threshold
    }
}

fun Graph.filterAtThreshold(threshold: Double, baseId: String? = this.id, i: Int = 0): Graph {
    return subgraph(
        edgeFilter = AbstractEdgeThresholdGraphProducer.EdgeThresholdFilter(threshold),
        id = "$baseId-$i"
    )
}
