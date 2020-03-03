package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

class DegreeMetric(params: MetricParams) : Metric(id) {

    companion object : MetricInfo() {
        override val id = "Degree"
        override val factory = ::DegreeMetric
    }

    override suspend fun evaluate0(graph: Graph) {
        graph.getEachNode<Node>().forEach { node ->
            node.addAttribute(id, node.degree)
        }
    }
}
