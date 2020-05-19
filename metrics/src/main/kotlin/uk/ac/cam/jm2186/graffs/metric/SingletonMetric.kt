package uk.ac.cam.jm2186.graffs.metric

import org.apache.commons.lang3.time.StopWatch
import org.graphstream.graph.Graph

abstract class SingletonMetric(override val id: MetricId) : Metric, MetricInfo {

    // self-factory for singleton subclasses
    override val factory: MetricFactory = { this }

    protected abstract fun evaluate0(graph: Graph)

    /**
     * Evaluate the metric. Assumes all required metrics have been computed for the graph.
     */
    override fun evaluate(graph: Graph): MetricResult? {
        return if (graph.hasAttribute(id)) {
            // Avoid calculating this metric if already calculated
            null
        } else {
            val stopWatch = StopWatch()
            stopWatch.start()
            evaluate0(graph)
            stopWatch.stop()

            if (!graph.hasAttribute(id)) {
                graph.addAttribute(id)
                MetricResult.Unit(stopWatch.time)
            } else {
                val value = graph.getAttribute<Double>(id)
                MetricResult.Double(value, stopWatch.time)
            }
        }
    }

}
