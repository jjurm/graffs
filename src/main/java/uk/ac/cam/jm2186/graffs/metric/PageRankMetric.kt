package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.PageRank
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.ATTRIBUTE_NAME_NODE_VALUE
import uk.ac.cam.jm2186.graffs.storage.model.MetricExperiment

class PageRankMetric(private val dampingFactor: Double) : Metric {

    class Factory : MetricFactory {
        override fun createMetric(params: List<Number>) = PageRankMetric(
            dampingFactor = params.getOrNull(0) as Double? ?: PageRank.DEFAULT_DAMPING_FACTOR
        )
    }

    override fun evaluate(graph: Graph): MetricResult {
        val alg = PageRank(dampingFactor, PageRank.DEFAULT_PRECISION, ATTRIBUTE_NAME_NODE_VALUE)
        alg.init(graph)
        alg.compute()

        return null to MetricExperiment.writeValuesGraph(graph)
    }
}
