package uk.ac.cam.jm2186.graffs.robustness

import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.getNumberAttribute
import java.util.HashMap

private fun extractList(graph: Graph, attributeName: String): List<String> {
    return graph.sortedBy { node ->
        node.getNumberAttribute(attributeName)
    }.map { it.id }
}

class AttributeNodeRanking internal constructor(list: List<String>) : NodeRanking,
    List<String> by list {
    constructor(graph: Graph, attributeName: String) : this(extractList(graph, attributeName))

    private val map = HashMap<String, Rank>()

    init {
        forEachIndexed { index, nodeId ->
            map[nodeId] = Rank(index)
        }
    }

    override fun getRank(node: String) = map.getValue(node)
}

fun OverallNodeRanking(rankings: List<AttributeNodeRanking>): NodeRanking {
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
    return AttributeNodeRanking(list)
}
