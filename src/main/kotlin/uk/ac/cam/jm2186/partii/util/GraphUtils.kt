package uk.ac.cam.jm2186.partii.util

import org.graphstream.graph.Graph
import org.graphstream.stream.file.FileSinkDGS
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

fun Graph.stringRepresentation(): String {
    val fs = FileSinkDGS()
    val baos = ByteArrayOutputStream()
    fs.writeAll(this, baos)
    return baos.toString(StandardCharsets.UTF_8.name())
}
