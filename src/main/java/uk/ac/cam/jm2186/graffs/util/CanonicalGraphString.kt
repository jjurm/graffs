@file:JvmName("CanonicalGraphString")

package uk.ac.cam.jm2186.graffs.util

import org.graphstream.graph.Edge
import org.graphstream.graph.Element
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import java.io.StringWriter
import java.io.Writer

/**
 * Echo nodes, edges and their attributes in a compact format.
 */
fun Graph.stringRepresentation(): String {
    val writer = StringWriter()
    exportGraph(this, writer)
    return writer.toString()
}

private fun exportGraph(graph: Graph, out: Writer) {
    fun exportAttributes(element: Element): String {
        return if (element.attributeCount > 0) {
            element.attributeKeySet
                .joinToString(",", "{", "}") { "$it=${element.getAttribute<Any>(it)}" }
        } else ""
    }

    fun writeNode(node: Node, attributes: Boolean = false) {
        out.write(node.id)
        if (attributes) out.write(exportAttributes(node))
    }

    fun writeEdge(edge: Edge, attributes: Boolean = false) {
        out.write(edge.id)
        if (attributes) out.write(exportAttributes(edge))
    }

    out.write(exportAttributes(graph))
    var newline = graph.attributeCount > 0

    val processed = HashSet<Node>()
    for (node in graph) {
        if (newline) out.write("\n")
        newline = true

        writeNode(node, attributes = true)

        val edgesToPrint = node.getEdgeSet<Edge>()
            .map {
                when {
                    processed.contains(it.getSourceNode()) -> it.getTargetNode<Node>() to it
                    processed.contains(it.getTargetNode()) -> it.getSourceNode<Node>() to it
                    else -> null
                }
            }
            .filterNotNull()
            .iterator()
        if (edgesToPrint.hasNext()) {
            out.write(" [")
            while (edgesToPrint.hasNext()) {
                val (otherNode, edge) = edgesToPrint.next()
                writeNode(otherNode)
                out.write("->")
                writeEdge(edge, attributes = true)
                if (edgesToPrint.hasNext()) out.write(",")
            }
            out.write("]")
        }

        processed.add(node)
    }
}

