package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Graph

object LocalClusteringMetric : Metric("LocalClustering"), MetricInfo {
    override val isNodeMetric get() = true

    override suspend fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val clustering = Toolkit.clusteringCoefficient(node)
            node.setAttribute(id, clustering)
        }
    }
}
