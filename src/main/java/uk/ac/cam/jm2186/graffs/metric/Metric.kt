package uk.ac.cam.jm2186.graffs.metric

import kotlinx.coroutines.CompletableDeferred
import org.graphstream.graph.Graph
import java.io.Serializable

typealias MetricId = String

abstract class Metric(val id: MetricId) : Serializable {

    companion object {
        val map: Map<MetricId, MetricInfo> = listOf(
            AverageDegreeMetric,
            DegreeMetric,
            BetweennessCentralityMetric,
            PageRankMetric,
            DangalchevClosenessCentralityMetric
        ).map { it.id to it }.toMap()

    }

    protected abstract suspend fun evaluate0(graph: Graph)

    /**
     * Evaluate the metric. Assumes all required metrics have been computed for the graph.
     */
    final suspend fun evaluate(graph: Graph): Boolean {
        if (graph.hasAttribute(id)) {
            // Avoid calculating this metric if already calculated
            return false
        } else {
            evaluate0(graph)
            if (!graph.hasAttribute(id))
                graph.addAttribute(id)
            return true
        }
    }

}

typealias MetricParams = List<Double>
typealias MetricFactory = (params: MetricParams) -> Metric

abstract class MetricInfo {
    abstract val id: MetricId
    abstract val factory: MetricFactory

    val dependencies: Set<MetricInfo> get() = setOf()
}
