package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.BetweennessCentrality
import org.graphstream.graph.Graph

object BetweennessCentralityMetric : SingletonMetric("Betweenness") {
    override val isNodeMetric get() = true
    override val attributeName = "B"

    override fun evaluate0(graph: Graph) {
        val alg = object : BetweennessCentrality(attributeName) {
            override fun compute() {
                super.compute()
                graph.forEach { node ->
                    node.removeAttribute(predAttributeName)
                    node.removeAttribute(sigmaAttributeName)
                    node.removeAttribute(distAttributeName)
                    node.removeAttribute(deltaAttributeName)
                }
            }
        }
        alg.computeEdgeCentrality(false)
        alg.init(graph)
        alg.compute()
    }
}

