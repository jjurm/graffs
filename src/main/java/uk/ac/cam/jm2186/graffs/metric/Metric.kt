package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import java.io.Serializable

typealias MetricId = String

abstract class Metric(val id: MetricId) : Serializable {

    companion object {
        val map: Map<MetricId, MetricInfo> = listOf<MetricInfo>(
            AverageDegreeMetric,
            DegreeMetric,
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
    suspend fun evaluate(graph: Graph): Boolean {
        return if (graph.hasAttribute(id)) {
            // Avoid calculating this metric if already calculated
            false
        } else {
            evaluate0(graph)
            if (!graph.hasAttribute(id))
                graph.addAttribute(id)
            true
        }
    }

}

typealias MetricFactory = () -> Metric

interface MetricInfo {
    val id: MetricId
    val factory: MetricFactory

    val dependencies: Set<MetricInfo> get() = setOf()
}
