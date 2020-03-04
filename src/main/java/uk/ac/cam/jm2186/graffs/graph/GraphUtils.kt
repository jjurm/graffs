package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Element
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file.FileSource
import java.io.InputStream

const val ATTRIBUTE_NAME_EDGE_WEIGHT = "w"

fun FileSource.readGraph(inputStream: InputStream, id: String): SingleGraph {
    val graph = SingleGraph(id, false, false)
    addSink(graph)
    readAll(inputStream)
    removeSink(graph)
    return graph
}

fun Element.getDoubleAttribute(attributeName: String) = getAttribute<Double?>(attributeName)
    ?: throw IllegalStateException("${this::class.simpleName} `${id}` has no attribute `${attributeName}`")
