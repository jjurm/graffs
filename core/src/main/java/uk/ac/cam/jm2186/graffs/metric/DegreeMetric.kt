package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

object DegreeMetric : Metric("Degree"), MetricInfo {
    override val isNodeMetric get() = true

    override suspend fun evaluate0(graph: Graph) {
        graph.getEachNode<Node>().forEach { node ->
            node.addAttribute(id, node.degree)
        }
    }
}
