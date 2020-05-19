package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph

object DegreeMetric : SingletonMetric("Degree") {
    override val isNodeMetric get() = true
    override val attributeName = "D"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            node.addAttribute(attributeName, node.degree)
        }
    }
}
