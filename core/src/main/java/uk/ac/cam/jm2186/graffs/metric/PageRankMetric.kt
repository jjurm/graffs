package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.PageRank
import org.graphstream.graph.Graph

object PageRankMetric : SingletonMetric("PageRank") {
    override val isNodeMetric get() = true
    override val attributeName = "P"

    override fun evaluate0(graph: Graph) {
        val alg = PageRank(PageRank.DEFAULT_DAMPING_FACTOR, PageRank.DEFAULT_PRECISION, attributeName)
        alg.init(graph)
        alg.compute()
    }
}
