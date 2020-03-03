package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.measure.AbstractCentrality
import org.graphstream.algorithm.measure.ClosenessCentrality
import org.graphstream.graph.Graph

class DangalchevClosenessCentralityMetric(params: MetricParams) : Metric(id) {

    companion object : MetricInfo() {
        override val id = "DangalchevCloseness"
        override val factory = ::DangalchevClosenessCentralityMetric
    }

    override suspend fun evaluate0(graph: Graph) {
        val alg = ClosenessCentrality(id, AbstractCentrality.NormalizationMode.NONE, true, true)
        alg.init(graph)
        alg.compute()
    }
}
