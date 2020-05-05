package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.BetweennessCentrality
import org.graphstream.graph.Graph

object BetweennessCentralityMetric : SingletonMetric("Betweenness") {
    override val isNodeMetric get() = true

    override fun evaluate0(graph: Graph) {
        val alg = BetweennessCentrality(attributeName)
        alg.computeEdgeCentrality(false)
        alg.init(graph)
        alg.compute()
    }
}

