package uk.ac.cam.jm2186.graffs.robustness

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.getNumberAttribute

class GraphAttributeNodeRanking(val graph: Graph, attributeName: String) :
    BaseNodeRanking(
        orderedNodes = graph.sortedBy { node ->
            node.getNumberAttribute(attributeName)
        }.map { it.id }
    ) {
}
