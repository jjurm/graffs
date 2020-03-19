package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.PageRank
import org.graphstream.graph.Graph

object PageRankMetric : Metric("PageRank"), MetricInfo {
    override val isNodeMetric get() = true

    override suspend fun evaluate0(graph: Graph) {
        val alg = PageRank(PageRank.DEFAULT_DAMPING_FACTOR, PageRank.DEFAULT_PRECISION, id)
        alg.init(graph)
        alg.compute()
    }
}
