package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Edge
import org.graphstream.graph.Element
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.DefaultGraph
import org.graphstream.graph.implementations.Graphs
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.GraphReplay
import org.graphstream.stream.file.FileSource
import org.graphstream.ui.layout.Layout
import org.graphstream.util.Filter
import org.graphstream.util.Filters
import java.io.InputStream

const val ATTRIBUTE_NAME_EDGE_WEIGHT = "w"

fun FileSource.readGraph(inputStream: InputStream, id: String): SingleGraph {
    val graph = SingleGraph(id, false, false)
    addSink(graph)
    readAll(inputStream)
    removeSink(graph)
    return graph
}

fun Graph.copy() = Graphs.clone(this)

fun Graph.subgraph(
    nodeFilter: Filter<Node> = Filters.trueFilter(),
    edgeFilter: Filter<Edge> = Filters.trueFilter()
): Graph {
    val replay = FilteredGraphReplay("$id-replay", nodeFilter, edgeFilter)
    val replayed = DefaultGraph(id)
    replay.addSink(replayed)
    replay.replay(this)
    replay.removeSink(replayed)
    return replayed
}

fun Graph.subgraph(
    nodeSet: Collection<Node>? = null,
    edgeSet: Collection<Edge>? = null
) = subgraph(
    nodeFilter = nodeSet?.let { set -> Filter<Node> { it in set } } ?: Filters.trueFilter(),
    edgeFilter = edgeSet?.let { set -> Filter<Edge> { it in set } } ?: Filters.trueFilter()
)

fun Element.getNumberAttribute(attributeName: String): Double {
    val number = getNumber(attributeName)
    if (number.isNaN()) {
        throw IllegalStateException("${this::class.simpleName} `${id}` has no attribute `${attributeName}`")
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
