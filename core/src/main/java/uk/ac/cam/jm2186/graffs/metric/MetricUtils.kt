package uk.ac.cam.jm2186.graffs.metric

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import uk.ac.cam.jm2186.graffs.graph.getNumberAttribute

fun CoroutineScope.evaluateMetricsAsync(
    metrics: Collection<Pair<MetricInfo, Metric>>,
    getGraph: () -> Graph,
    evaluateAndLog: suspend (metricEvaluation: suspend () -> MetricResult?, metric: MetricInfo) -> Unit = { eval, _ -> eval() },
    storeResults: suspend (graph: Graph) -> Unit = { }
): Deferred<Unit> {
    val graphDeferred = async { getGraph() }
    val graphMutex = Mutex()

    val jobs = HashMap<MetricInfo, Deferred<Unit>>()
    metrics.forEach { (metricInfo, metric) ->
        val job = async(start = CoroutineStart.LAZY) {
            // make sure that all dependencies are computed first
            val dependencies = metricInfo.dependencies.map { jobs.getValue(it) }
            dependencies.awaitAll()

            // now compute the current metric
            val graph = graphDeferred.await()
            graphMutex.withLock {
                evaluateAndLog({ metric.evaluate(graph) }, metricInfo)
            }
        }
        jobs[metricInfo] = job
    }

    // start and wait for all jobs
    val graphJob = async(start = CoroutineStart.LAZY) {
        jobs.values.awaitAll()
        // store the result
        storeResults(graphDeferred.await())
    }
    return graphJob
}

/**
 * Use this only to evaluate just this metric, just on the given graph (inefficient when in bulk).
 */
fun MetricInfo.evaluateSingle(graph: Graph) {
    val metric = this
    runBlocking {
        evaluateMetricsAsync(
            metrics = listOf(metric to metric.factory()),
            getGraph = { graph }
        ).await()
    }
}

fun Node.getMetricValue(metric: MetricInfo): Double = getNumberAttribute(metric.attributeName)
