package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.BetweennessCentrality
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.ATTRIBUTE_NAME_NODE_VALUE
import uk.ac.cam.jm2186.graffs.metric.Metric.Companion.removeNodeAttributesExceptV
import uk.ac.cam.jm2186.graffs.storage.model.MetricExperiment

class BetweennessCentralityMetric : Metric {

    class Factory : MetricFactory {
        override fun createMetric(params: List<Number>) = BetweennessCentralityMetric()
    }

    override fun evaluate(graph: Graph): MetricResult {
        val alg = BetweennessCentrality(ATTRIBUTE_NAME_NODE_VALUE)
        alg.computeEdgeCentrality(false)
        alg.init(graph)
        alg.compute()
        removeNodeAttributesExceptV(graph)

        return null to graph
    }
}

