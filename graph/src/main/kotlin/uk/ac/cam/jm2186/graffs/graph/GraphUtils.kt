package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Edge
import org.graphstream.graph.Element
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.DefaultGraph
import org.graphstream.graph.implementations.Graphs
import org.graphstream.stream.GraphReplay
import org.graphstream.ui.layout.Layout
import org.graphstream.util.Filter
import org.graphstream.util.Filters


fun Graph.copy() = Graphs.clone(this)

fun Graph.hasWeights() = getEdgeSet<Edge>().firstOrNull()?.hasAttribute(EDGE_ATTRIBUTE_WEIGHT) ?: false

fun Graph.subgraph(
    nodeFilter: Filter<Node> = Filters.trueFilter(),
    edgeFilter: Filter<Edge> = Filters.trueFilter(),
    id: String = this.id
): Graph {
    val replay = FilteredGraphReplay("${this.id}-replay", nodeFilter, edgeFilter)
    val replayed = DefaultGraph(id)
    replay.addSink(replayed)
    replay.replay(this)
    replay.removeSink(replayed)
    return replayed
}

fun Graph.subgraph(
    nodeSet: Collection<Node>? = null,
    edgeSet: Collection<Edge>? = null,
    id: String = this.id
) = subgraph(
    nodeFilter = nodeSet?.let { set -> Filter<Node> { it in set } } ?: Filters.trueFilter(),
    edgeFilter = edgeSet?.let { set -> Filter<Edge> { it in set } } ?: Filters.trueFilter(),
    id = id
)

fun Element.getNumberAttribute(attributeName: String): Double {
    val number = getNumber(attributeName)
    if (number.isNaN()) {
        throw IllegalStateException("${this::class.simpleName} `${id}` has no number attribute `${attributeName}`")
    }
    return number
}

fun Element.appendAttribute(attributeName: String, vararg newValues: Any?) {
    val old = getAttribute<Any?>(attributeName)
    val newList: Array<out Any?> = when (old) {
        null -> newValues
        is Array<*> -> arrayOf(*old, *newValues)
        else -> arrayOf(old, *newValues)
    }
    setAttribute(attributeName, *newList)
}

fun Element.style(styleCss: String) {
    val old = getAttribute<String?>("ui.style") ?: ""
    setAttribute("ui.style", old + styleCss)
}

fun Graph.computeLayout(layout: Layout, limit: Double) {
    // Listen to layout output
    layout.addAttributeSink(this)

    // Replay this graph to the layout
    val replay = GraphReplay("$id-layout-replay")
    replay.addSink(layout)
    replay.replay(this)
    replay.removeSink(layout)

    // Stabilise
    while (layout.stabilization < limit) layout.compute()

    // Stop listening to layout output
    layout.removeAttributeSink(this)
}
