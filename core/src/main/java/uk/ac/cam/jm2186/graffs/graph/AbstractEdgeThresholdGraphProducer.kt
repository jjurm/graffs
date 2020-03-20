package uk.ac.cam.jm2186.graffs.graph

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.util.Filter
import uk.ac.cam.jm2186.graffs.db.model.DistortedGraph
import uk.ac.cam.jm2186.graffs.util.FilteredGraphReplay

abstract class AbstractEdgeThresholdGraphProducer(
    val getThresholds: (n: Int) -> List<Double>
) : GraphProducer {

    companion object {
        const val ATTRIBUTE_EDGE_THRESHOLD = "edgeThreshold"
    }

    override fun produce(
        sourceGraph: Graph,
        n: Int,
        coroutineScope: CoroutineScope
    ): List<Deferred<DistortedGraph>> {
        val thresholds = getThresholds(n)
        val baseId = sourceGraph.id + "-" + this::class.simpleName

        return thresholds.mapIndexed { i, threshold ->
            coroutineScope.async {
                val replay = FilteredGraphReplay("$baseId-$i-replay", edgeFilter = EdgeThresholdFilter(threshold))
                val graph = SingleGraph("$baseId-$i")
                replay.addSink(graph)
                replay.replay(sourceGraph)
                graph.setAttribute(ATTRIBUTE_EDGE_THRESHOLD, threshold)
                DistortedGraph(threshold.hashCode().toLong(), graph)
            }
        }
    }

    private inner class EdgeThresholdFilter(val threshold: Double) : Filter<Edge> {
        override fun isAvailable(e: Edge): Boolean =
            e.getAttribute<Double>(ATTRIBUTE_NAME_EDGE_WEIGHT)!! > threshold
    }
}
