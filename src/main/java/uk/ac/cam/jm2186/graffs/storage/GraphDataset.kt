package uk.ac.cam.jm2186.graffs.storage

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.FileSourceEdge2
import uk.ac.cam.jm2186.graffs.graph.readGraph
import java.io.File


typealias GraphDatasetId = String

class GraphDataset(val id: GraphDatasetId, validate: Boolean = false) {

    init {
        if (validate && !File(datasetDirectory, id).exists()) {
            throw IllegalArgumentException("Dataset $id does not exist in the `$DATASET_DIRECTORY_NAME` directory (working dir: $workingDirectory)")
        }
    }

    companion object {

        @JvmStatic
        var workingDirectory = System.getProperty("user.dir")

        const val DATASET_DIRECTORY_NAME = "data"
        val datasetDirectory get() = File(File(workingDirectory), DATASET_DIRECTORY_NAME)

        /**
         * Returns list of datasets from subdirectories of `data` folder that are not hidden and not starting with ".".
         * Returns null if the `data` directory doesn't exist.
         */
        fun getAvailableDatasets(): List<GraphDataset>? =
            datasetDirectory.listFiles { dir, name ->
                val f = File(dir, name)
                f.isDirectory && !f.isHidden && !name.startsWith(".")
            }?.map { GraphDataset(it.name) }

        private val loadedGraphs: MutableMap<GraphDataset, Graph> = mutableMapOf()

    }

    /**
     * Loads information about the dataset contained in the `info.txt` file
     */
    fun loadInfo(): String? {
        val file = File(File(datasetDirectory, id), "info.txt")
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
        val file = File(File(datasetDirectory, id), "edges.txt")
        return@getOrPut FileSourceEdge2(false).readGraph(file.inputStream(), id)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GraphDataset) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
