package uk.ac.cam.jm2186.partii.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

class AverageDegreeMetric : Metric<Double> {

    class Factory : MetricFactory<Double> {
        override fun createMetric(sourceGraph: Graph, params: List<Number>) = AverageDegreeMetric()
    }

    override fun evaluate(graph: Graph): Double {
        return graph.getNodeSet<Node>().map { it.degree }.average()
    }
}
