package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.measure.AbstractCentrality
import org.graphstream.algorithm.measure.ClosenessCentrality
import org.graphstream.graph.Graph

object DangalchevClosenessCentralityMetric : SingletonMetric("DangalchevCloseness") {
    override val isNodeMetric get() = true

    override fun evaluate0(graph: Graph) {
        val alg = ClosenessCentrality(id, AbstractCentrality.NormalizationMode.NONE, true, true)
        alg.init(graph)
        alg.compute()
    }
}
