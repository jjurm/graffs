package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.algorithm.APSP
import org.graphstream.graph.Graph

object ASPSMetric : SingletonMetric("ASPS") {
    override val isNodeMetric get() = false

    override fun evaluate0(graph: Graph) {
        val apsp = APSP(graph, null, false)
        apsp.init(graph)
        apsp.compute()
    }
}
