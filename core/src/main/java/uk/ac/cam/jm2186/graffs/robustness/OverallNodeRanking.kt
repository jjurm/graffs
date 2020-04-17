package uk.ac.cam.jm2186.graffs.robustness

import kotlinx.coroutines.*
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.db.model.GraphCollection

class OverallNodeRanking internal constructor(
    val rankings: List<GraphAttributeNodeRanking>,
    orderedNodes: List<String>
) : BaseNodeRanking(orderedNodes) {

    companion object {
        private suspend fun overallNodeRanking(
            rankings: List<BaseNodeRanking>
        ): List<String> {
            // Accumulate ranks of each node
            val allRanks: MultiValuedMap<String, Rank> = ArrayListValuedHashMap<String, Rank>()
            rankings.forEach { ranking ->
                ranking.forEachRanked { rank, nodeId ->
                    allRanks.put(nodeId, rank)
                }
            }
            // Rank nodes by their average ranks
            val list = coroutineScope {
                allRanks.asMap()
                    .map { (nodeId, values) ->
                        async { nodeId to values.map { it.rankValue }.average() }
                    }
                    .awaitAll()
                    .sortedBy(Pair<String, Double>::second)
                    .map { it.first }
            }
            return list
        }

        suspend operator fun invoke(rankings: List<GraphAttributeNodeRanking>) =
            OverallNodeRanking(rankings, overallNodeRanking(rankings))

        suspend operator fun invoke(graphCollection: GraphCollection, metric: MetricInfo) =
            this@Companion(
                // Compute rankings of each graph asynchronously
                rankings = coroutineScope {
                    graphCollection.perturbedGraphs.map {
                        async {
                            GraphAttributeNodeRanking(it.graph, metric.attributeName)
                        }
                    }.awaitAll()
                }
            )

    }

}
