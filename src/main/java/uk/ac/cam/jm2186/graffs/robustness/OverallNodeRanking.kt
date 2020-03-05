package uk.ac.cam.jm2186.graffs.robustness

import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.storage.model.GraphCollection

class OverallNodeRanking internal constructor(
    val rankings: List<GraphAttributeNodeRanking>
) : BaseNodeRanking(overallNodeRanking(rankings)) {

    constructor(graphCollection: GraphCollection, metric: MetricInfo) : this(graphCollection.distortedGraphs.map {
        val graph = it.graph
        GraphAttributeNodeRanking(graph, metric.attributeName)
    })

    companion object {
        private fun overallNodeRanking(rankings: List<BaseNodeRanking>): List<String> {
            // Accumulate ranks of each node
            val allRanks: MultiValuedMap<String, Rank> = ArrayListValuedHashMap<String, Rank>()
            rankings.forEach { ranking ->
                ranking.forEachRanked { rank, nodeId ->
                    allRanks.put(nodeId, rank)
                }
            }
            // Rank nodes by their average ranks
            val list = allRanks.asMap()
                .map { (nodeId, values) ->
                    nodeId to values.map { it.rankValue }.average()
                }
                .sortedBy(Pair<String, Double>::second)
                .map { it.first }
            return list
        }
    }

}
