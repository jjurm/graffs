package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.BetweennessCentrality
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.metric.Metric.Companion.removeNodeAttributesExceptV
import uk.ac.cam.jm2186.graffs.storage.model.MetricExperiment

class BetweennessCentralityMetric : Metric {

    class Factory : MetricFactory {
        override fun createMetric(params: List<Number>) = BetweennessCentralityMetric()
    }

    override fun evaluate(graph: Graph): MetricResult {
        val alg = BetweennessCentrality("v")
        alg.computeEdgeCentrality(false)
        alg.init(graph)
        alg.compute()
        removeNodeAttributesExceptV(graph)

        return null to MetricExperiment.writeValuesGraph(graph)
    }
}

