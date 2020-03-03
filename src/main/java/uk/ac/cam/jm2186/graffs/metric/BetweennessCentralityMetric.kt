package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.BetweennessCentrality
import org.graphstream.graph.Graph

class BetweennessCentralityMetric(params: MetricParams) : Metric(id) {

    companion object : MetricInfo() {
        override val id = "Betweenness"
        override val factory = ::BetweennessCentralityMetric
    }

    override suspend fun evaluate0(graph: Graph) {
        val alg = BetweennessCentrality(id)
        alg.computeEdgeCentrality(false)
        alg.init(graph)
        alg.compute()
    }
}

