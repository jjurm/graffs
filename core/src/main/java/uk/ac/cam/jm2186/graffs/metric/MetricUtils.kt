package uk.ac.cam.jm2186.graffs.metric

import kotlinx.coroutines.*
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import uk.ac.cam.jm2186.graffs.graph.getNumberAttribute

fun CoroutineScope.evaluateMetricsAsync(
    metrics: Collection<MetricInfo>,
    getGraph: () -> Graph,
    evaluateAndLog: suspend (metricEvaluation: suspend () -> MetricResult?, metric: MetricInfo) -> Unit = { eval, _ -> eval() },
    storeResults: suspend (graph: Graph) -> Unit = { }
): Deferred<Unit> {
    val graphDeferred = async { getGraph() }

    // include also all dependencies needed to compute
    val metricsToCompute: MutableList<MetricInfo> = mutableListOf()
    fun addWithDependencies(metric: MetricInfo) {
        if (metric in metricsToCompute) return
        metric.dependencies.forEach { addWithDependencies(it) }
        metricsToCompute += metric
    }
    metrics.forEach { metric -> addWithDependencies(metric) }

    val job = async(start = CoroutineStart.LAZY) {
        metricsToCompute.forEach { metricInfo ->
            val graph = graphDeferred.await()
            val metric = metricInfo.factory()
            evaluateAndLog({ metric.evaluate(graph) }, metricInfo)
        }
        // store the result
        storeResults(graphDeferred.await())
    }
    return job
}

/**
 * Use this only to evaluate just this metric, just on the given graph (inefficient when in bulk).
 */
fun MetricInfo.evaluateSingle(graph: Graph) {
    val metric = this
    runBlocking {
        evaluateMetricsAsync(
            metrics = listOf(metric),
            getGraph = { graph }
        ).await()
    }
}

fun Node.getMetricValue(metric: MetricInfo): Double = getNumberAttribute(metric.attributeName)
