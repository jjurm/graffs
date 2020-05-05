package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Graph

object LocalClusteringMetric : SingletonMetric("LocalClustering") {
    override val isNodeMetric get() = true
    override val attributeName = "LC"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val clustering = Toolkit.clusteringCoefficient(node)
            node.setAttribute(attributeName, clustering)
        }
    }
}
