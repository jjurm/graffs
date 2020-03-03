package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

class AverageDegreeMetric(params: MetricParams) : Metric(id) {

    companion object : MetricInfo() {
        override val id = "AverageDegree"
        override val factory = ::AverageDegreeMetric
    }

    override suspend fun evaluate0(graph: Graph) {
        val average = graph.getNodeSet<Node>().map { it.degree }.average()
        graph.setAttribute(id, average)
    }
}
