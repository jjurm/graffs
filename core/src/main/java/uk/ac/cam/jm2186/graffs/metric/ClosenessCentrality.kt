package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.APSP.APSPInfo
import org.graphstream.graph.Graph

object ClosenessCentrality : SingletonMetric("Closeness") {
    override val isNodeMetric get() = true
    override val dependencies: Set<MetricInfo> = setOf(APSPMetric)
    override val attributeName = "C"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val info = node.getAttribute<APSPInfo>(APSPInfo.ATTRIBUTE_NAME)!!
            val sum = graph.asSequence().map { other ->
                if (node.id != other.id) {
                    info.getLengthTo(other.id).takeIf { it >= 0 } ?: graph.nodeCount.toDouble()
                } else 0.0
            }.sum()
            val closeness = 1 / sum
            node.addAttribute(attributeName, closeness)
        }
    }
}
