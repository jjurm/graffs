package uk.ac.cam.jm2186.partii.storage

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import uk.ac.cam.jm2186.partii.graph.FileSourceEdge2
import java.io.File
import java.io.Serializable

class GraphDataset(val id: String) : Serializable {

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

    /**
     * Loads information about the dataset contained in the `info.txt` file
     */
    fun loadInfo(): String? {
        val file = File("${DATASET_DIRECTORY}/$id/info.txt")
        return if (file.exists()) {
            file.readText()
        } else {
            null
        }
    }

    /**
     * Loads the dataset graph contained in the `edges.txt` file
     */
    @Throws(IllegalArgumentException::class)
    fun loadGraph(): Graph = loadedGraphs.getOrPut(this) {
        val fileSource = FileSourceEdge2(false)
        val filename = "${DATASET_DIRECTORY}/$id/edges.txt"
        val file = File(filename)
        if (file.exists()) {
            val graph = SingleGraph(id, false, false)
            fileSource.addSink(graph)
            fileSource.readAll(file.inputStream())
            fileSource.removeSink(graph)
            return@getOrPut graph
        } else {
            throw IllegalArgumentException("Dataset $id does not exist in the `${DATASET_DIRECTORY}` directory.")
        }
    }
}
