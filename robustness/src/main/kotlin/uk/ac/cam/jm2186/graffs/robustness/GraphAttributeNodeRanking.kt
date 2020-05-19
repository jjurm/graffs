package uk.ac.cam.jm2186.graffs.robustness

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

class GraphAttributeNodeRanking(val graph: Graph, attributeName: String) :
    BaseNodeRanking(
        orderedNodes = graph.sortedBy { node ->
            node.getNumberAttribute(attributeName)
        }.map { it.id }
    )

fun Node.getNumberAttribute(attributeName: String): Double {
    val number = getNumber(attributeName)
    if (number.isNaN()) {
        throw IllegalStateException("${this::class.simpleName} `${id}` has NaN number attribute `${attributeName}`")
    }
    return number
}
