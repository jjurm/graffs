package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file.FileSource
import java.io.InputStream

fun FileSource.readGraph(inputStream: InputStream, id: String): SingleGraph {
    val graph = SingleGraph(id, false, false)
    addSink(graph)
    readAll(inputStream)
    removeSink(graph)
    return graph
}
