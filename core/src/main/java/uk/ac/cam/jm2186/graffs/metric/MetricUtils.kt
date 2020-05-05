package uk.ac.cam.jm2186.graffs.metric

import kotlinx.coroutines.*
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import uk.ac.cam.jm2186.graffs.graph.getNumberAttribute

fun Collection<MetricInfo>.topologicalOrderWithDependencies(): List<MetricInfo> {
    // include also all dependencies needed to compute
    val metricsToCompute: MutableList<MetricInfo> = mutableListOf()
    fun addWithDependencies(metric: MetricInfo) {
        if (metric in metricsToCompute) return
        metric.dependencies.forEach { addWithDependencies(it) }
        metricsToCompute += metric
    }
    forEach { metric -> addWithDependencies(metric) }
    return metricsToCompute
}

fun CoroutineScope.evaluateMetricsAsync(
    metrics: Collection<MetricInfo>,
    getGraph: () -> Graph,
    evaluateAndLog: suspend (metricEvaluation: suspend () -> MetricResult?, metric: MetricInfo) -> Unit = { eval, _ -> eval() },
    storeResults: suspend (graph: Graph) -> Unit = { }
): Deferred<Unit> {
    val graphDeferred = async { getGraph() }

    val metricsToCompute = metrics.topologicalOrderWithDependencies()

    return async(start = CoroutineStart.LAZY) {
        val metricList = metricsToCompute.map { it to it.factory() }
        val graph = graphDeferred.await()
        metricList.forEach { (metricInfo, metric) ->
            evaluateAndLog({ metric.evaluate(graph) }, metricInfo)
        }
        metricList.forEach { (_, metric) ->
            metric.cleanup(graph)
        }
        // store the result
        storeResults(graphDeferred.await())
    }
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
