package uk.ac.cam.jm2186.graffs.graph.storage

import org.graphstream.graph.Graph
import java.io.File
import java.io.IOException


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
        val datasetDirectory
            get() = File(
                File(workingDirectory),
                DATASET_DIRECTORY_NAME
            )

        private val loaders = listOf(
            RDataGraphLoader { name.endsWith(".RData") },
            EdgesGraphLoader { name == "edges.txt" },
            EdgesGraphLoader { name.endsWith(".txt") }
        )

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

    fun getFileCandidates(): Array<File> = File(datasetDirectory, id).listFiles { file -> file.name != "info.txt" }
        ?: throw IOException("`$DATASET_DIRECTORY_NAME/$id` is not a directory!")

    fun loadGraph(): Graph = loadedGraphs.getOrPut(this) {
        val fileCandidates = getFileCandidates()
        loaders.flatMap { loader ->
            fileCandidates
                .filter { loader.supportsFile(it) }
                .map { { loader.load(it, id) } }
        }.firstOrNull()?.invoke()
            ?: throw IOException("Could not load dataset `$id` - no suitable graph file found.")
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
