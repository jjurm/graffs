package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class SingletonMetricTest : TestOnGraph() {

    class SampleMetric(val lambda: (Graph) -> Unit) : SingletonMetric("Sample") {
        override val isNodeMetric = false

        override fun evaluate0(graph: Graph) {
            lambda(graph)
        }
    }

    @Test
    fun evaluateAlreadyCalculated() {
        graph.addAttribute("Sample")
        val m = SampleMetric {}
        assertNull(m.evaluate(graph))
    }

    @Test
    fun evaluateUnitResult() {
        val m = SampleMetric {}
        assertTrue(m.evaluate(graph) is MetricResult.Unit)
    }

    @Test
    fun evaluateNumResult() {
        val m = SampleMetric {
            it.addAttribute("Sample", 1.0)
        }
        val res = m.evaluate(graph)
        assertTrue(res is MetricResult.Double)
        assertEquals(1.0, (res as MetricResult.Double).value)
    }
}
