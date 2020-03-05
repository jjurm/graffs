package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

object AverageDegreeMetric : Metric("AverageDegree"), MetricInfo {
    override val isNodeMetric get() = false

    override suspend fun evaluate0(graph: Graph) {
        val average = graph.getNodeSet<Node>().map { it.degree }.average()
        graph.setAttribute(id, average)
    }
}
