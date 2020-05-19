package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph

object RedundancyMetric : SingletonMetric("Redundancy") {
    override val isNodeMetric get() = true
    override val dependencies: Set<MetricInfo> = setOf(LocalClusteringMetric)
    override val attributeName = "R"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val localClustering = node.getMetricValue(LocalClusteringMetric)
            // local clustering is 0 for degree <= 1
            val redundancy = localClustering * (node.degree - 1)
            node.addAttribute(attributeName, redundancy)
        }
    }
}
