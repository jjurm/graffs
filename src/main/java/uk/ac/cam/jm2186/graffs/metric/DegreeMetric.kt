package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import uk.ac.cam.jm2186.graffs.graph.ATTRIBUTE_NAME_NODE_VALUE

class DegreeMetric : Metric {

    class Factory : MetricFactory {
        override fun createMetric(params: List<Number>) = DegreeMetric()
    }

    override fun evaluate(graph: Graph): MetricResult {
        graph.getEachNode<Node>().forEach { node ->
            node.addAttribute(ATTRIBUTE_NAME_NODE_VALUE, node.degree)
        }
        return null to graph
    }
}
