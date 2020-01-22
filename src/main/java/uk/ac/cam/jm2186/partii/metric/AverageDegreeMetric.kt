package uk.ac.cam.jm2186.partii.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

class AverageDegreeMetric : Metric {

    class Factory : MetricFactory {
        override fun createMetric(params: List<Number>) = AverageDegreeMetric()
    }

    override fun evaluate(graph: Graph): MetricResult {
        val average = graph.getNodeSet<Node>().map { it.degree }.average()
        return average to null
    }
}
