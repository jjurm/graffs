package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

object RedundancyMetric : SingletonMetric("Redundancy") {
    override val isNodeMetric get() = true
    override val dependencies: Set<MetricInfo> = setOf(LocalClusteringMetric)

    override fun evaluate0(graph: Graph) {
        graph.getEachNode<Node>().forEach { node ->
            val localClustering = node.getMetricValue(LocalClusteringMetric)
            // local clustering is 0 for degree <= 1
            val redundancy = localClustering * (node.degree - 1)
            node.addAttribute(attributeName, redundancy)
        }
    }
}
