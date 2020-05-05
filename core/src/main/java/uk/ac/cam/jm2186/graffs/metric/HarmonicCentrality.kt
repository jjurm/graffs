package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.APSP
import org.graphstream.graph.Graph
import org.graphstream.graph.Node

object HarmonicCentrality : SingletonMetric("Harmonic") {
    override val isNodeMetric get() = true
    override val dependencies: Set<MetricInfo> = setOf(ASPSMetric)

    override fun evaluate0(graph: Graph) {
        graph.getEachNode<Node>().forEach { node ->
            val info = node.getAttribute<APSP.APSPInfo>(APSP.APSPInfo.ATTRIBUTE_NAME)!!
            val harmonicCentrality = graph.getEachNode<Node>().asSequence().map { other ->
                if (node.id != other.id) {
                    info.getLengthTo(other.id).takeIf { it > 0 }?.let { 1 / it } ?: 0.0
                } else 0.0
            }.sum()
            node.addAttribute(attributeName, harmonicCentrality)
        }
    }
}
