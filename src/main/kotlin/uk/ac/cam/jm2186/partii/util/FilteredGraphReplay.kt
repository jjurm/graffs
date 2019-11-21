package uk.ac.cam.jm2186.partii.util

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.stream.GraphReplay
import org.graphstream.util.Filter
import org.graphstream.util.Filters

class FilteredGraphReplay(
    id: String,
    private val nodeFilter: Filter<Node> = Filters.trueFilter(),
    private val edgeFilter: Filter<Edge> = Filters.trueFilter()
) : GraphReplay(id) {

    override fun replay(graph: Graph) {
        for (key in graph.attributeKeySet)
            sendGraphAttributeAdded(sourceId, key, graph.getAttribute(key))
        for (node in graph) if (nodeFilter.isAvailable(node)) {
            val nodeId = node.id
            sendNodeAdded(sourceId, nodeId)
            if (node.attributeCount > 0) for (key in node.attributeKeySet)
                sendNodeAttributeAdded(sourceId, nodeId, key, node.getAttribute(key))
        }
        for (edge in graph.getEachEdge<Edge>()) {
            if (edgeFilter.isAvailable(edge)) {
                val node0 = edge.getNode0<Node>()
                val node1 = edge.getNode1<Node>()
                if (nodeFilter.isAvailable(node0) && nodeFilter.isAvailable(node1)) {
                    val edgeId = edge.id
                    sendEdgeAdded(sourceId, edgeId, node0.id, node1.id, edge.isDirected)
                    if (edge.attributeCount > 0) for (key in edge.attributeKeySet)
                        sendEdgeAttributeAdded(sourceId, edgeId, key, edge.getAttribute(key))
                }
            }
        }
    }
}
