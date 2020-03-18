package uk.ac.cam.jm2186.graffs.graph.storage

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.readGraph
import java.io.File

internal class EdgesGraphLoader(fileFilter: File.() -> Boolean = { true }) : GraphLoader(fileFilter) {

    override fun load(file: File, id: String): Graph {
        return FileSourceEdge2(false).readGraph(file.inputStream(), id)
    }

}
