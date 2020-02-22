package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.measure.AbstractCentrality
import org.graphstream.algorithm.measure.ClosenessCentrality
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.ATTRIBUTE_NAME_NODE_VALUE
import uk.ac.cam.jm2186.graffs.metric.Metric.Companion.removeNodeAttributesExceptV

class ClosenessCentralityMetric(val dangalchev: Boolean) : Metric {

    class DangalchevFactory : MetricFactory {
        override fun createMetric(params: List<Number>) = ClosenessCentralityMetric(true)
    }

    override fun evaluate(graph: Graph): MetricResult {
        val alg = ClosenessCentrality(ATTRIBUTE_NAME_NODE_VALUE, AbstractCentrality.NormalizationMode.NONE, true, dangalchev)
        alg.init(graph)
        alg.compute()
        removeNodeAttributesExceptV(graph)

        return null to graph
    }
}
