package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.Graphs
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.util.Filter
import uk.ac.cam.jm2186.graffs.util.FilteredGraphReplay
import kotlin.random.Random

class EdgeThresholdGraphProducer(
    val sourceGraph: Graph,
    val thresholdLow: Double,
    val thresholdHigh: Double,
    seed: Long
) : GraphProducer {

    companion object {
        const val ID: GraphProducerId = "edge-threshold"
    }

    class Factory : GraphProducerFactory {
        override fun createGraphProducer(sourceGraph: Graph, seed: Long, params: List<Number>) =
            EdgeThresholdGraphProducer(
                sourceGraph,
                thresholdLow = params[0] as Double,
                thresholdHigh = params[1] as Double,
                seed = seed
            )
    }

    private val edgeThreshold = Random(seed).nextDouble(thresholdLow, thresholdHigh)
    private val id = sourceGraph.id + "-" + javaClass.name
    private var count = 0
    private val filteredReplay by lazy {
        FilteredGraphReplay(id, edgeFilter = EdgeThresholdFilter())
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
        val filter = EdgeThresholdFilter()
        cloned.getEachEdge<Edge>().removeAll { !filter.isAvailable(it) }
        return cloned
    }

    private inner class EdgeThresholdFilter : Filter<Edge> {
        override fun isAvailable(e: Edge): Boolean =
            e.getAttribute<Double>(ATTRIBUTE_NAME_EDGE_WEIGHT)!! > edgeThreshold
    }
}
