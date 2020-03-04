package uk.ac.cam.jm2186.graffs.robustness

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.getNumberAttribute
import java.util.*

private fun extractList(graph: Graph, attributeName: String): List<String> {
    return graph.sortedBy { node ->
        node.getNumberAttribute(attributeName)
    }.map { it.id }
}

open class AttributeNodeRanking internal constructor(list: List<String>) : NodeRanking, List<String> by list {
    constructor(graph: Graph, attributeName: String) : this(extractList(graph, attributeName))

    private val map = HashMap<String, Rank>()

    init {
        forEachIndexed { index, nodeId ->
            map[nodeId] = Rank(index)
        }
    }

    override fun getRank(node: String) = map.getValue(node)
}

