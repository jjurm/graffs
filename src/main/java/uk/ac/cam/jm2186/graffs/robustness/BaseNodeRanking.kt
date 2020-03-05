package uk.ac.cam.jm2186.graffs.robustness

import java.util.*

open class BaseNodeRanking internal constructor(orderedNodes: List<String>) : NodeRanking,
    List<String> by orderedNodes {

    private val map = HashMap<String, Rank>()

    init {
        forEachIndexed { index, nodeId ->
            map[nodeId] = Rank(index)
        }
    }

    override fun getRank(node: String) = map.getValue(node)
}

