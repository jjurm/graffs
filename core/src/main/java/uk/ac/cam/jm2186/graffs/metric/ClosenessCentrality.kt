package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.APSP.APSPInfo
import org.graphstream.graph.Graph
import org.graphstream.graph.Node

object ClosenessCentrality : SingletonMetric("Closeness") {
    override val isNodeMetric get() = true
    override val dependencies: Set<MetricInfo> = setOf(APSPMetric)

    override fun evaluate0(graph: Graph) {
        graph.getEachNode<Node>().forEach { node ->
            val info = node.getAttribute<APSPInfo>(APSPInfo.ATTRIBUTE_NAME)!!
            val sum = graph.getEachNode<Node>().asSequence().map { other ->
                if (node.id != other.id) {
                    info.getLengthTo(other.id).takeIf { it >= 0 } ?: graph.nodeCount.toDouble()
                } else 0.0
            }.sum()
            val closeness = 1 / sum
            node.addAttribute(attributeName, closeness)
        }
    }
}
