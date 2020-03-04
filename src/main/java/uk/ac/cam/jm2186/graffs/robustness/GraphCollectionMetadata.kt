package uk.ac.cam.jm2186.graffs.robustness

import kotlinx.coroutines.*
import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.storage.model.GraphCollection

/**
 * Lazily computes and caches intermediate results that may be used by multiple robustness calculations.
 */
class GraphCollectionMetadata(
    graphCollection: GraphCollection,
    metric: MetricInfo,
    coroutineScope: CoroutineScope
) {

    private val overallRanking = coroutineScope.async(start = CoroutineStart.LAZY) {
        OverallNodeRanking(graphCollection, metric)
    }

    suspend fun getOverallRanking() = overallRanking.await()

}

