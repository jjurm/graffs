package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.BetweennessCentrality
import org.graphstream.graph.Graph

object BetweennessCentralityMetric : Metric("Betweenness"), MetricInfo {

    override suspend fun evaluate0(graph: Graph) {
        val alg = BetweennessCentrality(id)
        alg.computeEdgeCentrality(false)
        alg.init(graph)
        alg.compute()
    }
}

