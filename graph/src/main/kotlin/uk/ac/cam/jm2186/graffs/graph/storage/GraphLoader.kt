package uk.ac.cam.jm2186.graffs.graph.storage

import org.graphstream.graph.Graph
import java.io.File

internal abstract class GraphLoader(private val fileFilter: File.() -> Boolean) {
    fun supportsFile(file: File): Boolean = fileFilter(file)
    abstract fun load(file: File, id: String): Graph
}
