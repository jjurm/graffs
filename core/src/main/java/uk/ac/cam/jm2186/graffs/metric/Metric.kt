package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph

typealias MetricId = String

interface Metric {
    fun evaluate(graph: Graph): MetricResult?

    companion object {
        val map: Map<MetricId, MetricInfo> by lazy {
            listOf<MetricInfo>(
                AverageDegreeMetric,
                DegreeMetric,
                LocalClusteringMetric,
                BetweennessCentralityMetric,
                PageRankMetric,
                DangalchevClosenessCentralityMetric
            ).map { it.id to it }.toMap()
        }
    }
}

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
            evaluate0(graph)
            if (!graph.hasAttribute(id)) {
                graph.addAttribute(id)
                MetricResult.Unit
            } else {
                val value = graph.getAttribute<Double>(id)
                MetricResult.Double(value)
            }
        }
    }

}

typealias MetricFactory = () -> Metric

sealed class MetricResult {
    object Unit : MetricResult() {
        override fun toString() = "[true]"
    }

    class Double(val value: kotlin.Double) : MetricResult() {
        override fun toString() = "%.2f".format(value)
    }
}

interface MetricInfo {
    val id: MetricId
    val factory: MetricFactory
    val isNodeMetric: Boolean

    val dependencies: Set<MetricInfo> get() = setOf()
    val attributeName: String get() = id
}
