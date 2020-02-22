package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.measure.AbstractCentrality
import org.graphstream.algorithm.measure.ClosenessCentrality
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.metric.Metric.Companion.removeNodeAttributesExceptV
import uk.ac.cam.jm2186.graffs.storage.model.MetricExperiment

class ClosenessCentralityMetric(val dangalchev: Boolean) : Metric {

    class DangalchevFactory : MetricFactory {
        override fun createMetric(params: List<Number>) = ClosenessCentralityMetric(true)
    }

    override fun evaluate(graph: Graph): MetricResult {
        val alg = ClosenessCentrality("v", AbstractCentrality.NormalizationMode.NONE, true, dangalchev)
        alg.init(graph)
        alg.compute()
        removeNodeAttributesExceptV(graph)

        return null to MetricExperiment.writeValuesGraph(graph)
    }
}
