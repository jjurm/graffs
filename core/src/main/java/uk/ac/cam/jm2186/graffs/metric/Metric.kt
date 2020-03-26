package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import java.io.Serializable

typealias MetricId = String

abstract class Metric(val id: MetricId) : Serializable {

    companion object {
        val map: Map<MetricId, MetricInfo> = listOf<MetricInfo>(
            AverageDegreeMetric,
            DegreeMetric,
            LocalClusteringMetric,
            BetweennessCentralityMetric,
            PageRankMetric,
            DangalchevClosenessCentralityMetric
        ).map { it.id to it }.toMap()
    }

    // self-factory for singleton subclasses
    open val factory: MetricFactory = { this }

    protected abstract suspend fun evaluate0(graph: Graph)

    /**
     * Evaluate the metric. Assumes all required metrics have been computed for the graph.
     */
    suspend fun evaluate(graph: Graph): MetricResult? {
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
