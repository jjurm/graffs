package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.PageRank
import org.graphstream.graph.Graph

class PageRankMetric(params: MetricParams) : Metric(id) {

    private val dampingFactor: Double = params.getOrNull(0) ?: PageRank.DEFAULT_DAMPING_FACTOR

    companion object : MetricInfo() {
        override val id = "PageRank"
        override val factory = ::PageRankMetric
    }

    override suspend fun evaluate0(graph: Graph) {
        val alg = PageRank(dampingFactor, PageRank.DEFAULT_PRECISION, id)
        alg.init(graph)
        alg.compute()
    }
}
