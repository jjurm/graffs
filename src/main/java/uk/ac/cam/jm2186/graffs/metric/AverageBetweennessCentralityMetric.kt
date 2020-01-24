package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

class AverageBetweennessCentralityMetric : Metric {

    class Factory : MetricFactory {
        override fun createMetric(params: List<Number>) = AverageBetweennessCentralityMetric()
    }

    override fun evaluate(graph: Graph): MetricResult {
        BetweennessCentralityMetric.compute(graph)

        val average = graph.getEachNode<Node>()
            .map<Node, Double> { node -> node.getAttribute("v") }
            .average()
        return average to null
    }
}
