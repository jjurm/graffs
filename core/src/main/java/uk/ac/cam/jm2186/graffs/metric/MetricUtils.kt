package uk.ac.cam.jm2186.graffs.metric

import kotlinx.coroutines.*
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import uk.ac.cam.jm2186.graffs.db.model.PerturbedGraph
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
    log: (metric: MetricInfo, result: MetricResult) -> Unit = { _, _ -> },
    storeResults: suspend (saveTo: (PerturbedGraph) -> Unit) -> Unit = { }
): Deferred<Unit> {
    val graphDeferred = async { getGraph() }

    val metricsToCompute = metrics.topologicalOrderWithDependencies()

    return async(start = CoroutineStart.LAZY) {
        val metricList = metricsToCompute.map { it to it.factory() }
        val graph = graphDeferred.await()
        val timings = metricList.mapNotNull { (metricInfo, metric) ->
            val result = metric.evaluate(graph)
            result?.let {
                log(metricInfo, result)
                metricInfo.id to result.time
            }
        }
        metricList.forEach { (_, metric) ->
            metric.cleanup(graph)
        }
        // store any new results
        if (timings.isNotEmpty()) {
            storeResults { perturbedGraph ->
                perturbedGraph.graph = graph
                perturbedGraph.addTimings(timings)
            }
        }
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
