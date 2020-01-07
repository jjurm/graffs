package uk.ac.cam.jm2186.partii.storage

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file.FileSourceEdge
import uk.ac.cam.jm2186.partii.graph.FileSourceEdge2
import java.io.File

class GraphDataset(val id: String) {

    companion object {

        const val DATASET_DIRECTORY = "data"

        /**
         * Returns list of datasets from subdirectories of `data` folder that are not hidden and not starting with ".".
         * Returns null if the `data` directory doesn't exist.
         */
        fun getAvailableDatasets(): List<GraphDataset>? =
            File(DATASET_DIRECTORY).listFiles { dir, name ->
                val f = File(dir, name)
                f.isDirectory && !f.isHidden && !name.startsWith(".")
            }?.map { GraphDataset(it.name) }

        private val loadedGraphs: MutableMap<GraphDataset, Graph> = mutableMapOf()

    }

    fun loadGraph(): Graph = loadedGraphs.getOrPut(this) {
        val fileSource = FileSourceEdge2(false)
        val filename = "data/$id/edges.txt"
        val graph = SingleGraph(id, false, false)
        fileSource.addSink(graph)
        fileSource.readAll(filename)
        fileSource.removeSink(graph)
        return@getOrPut graph
    }
}
