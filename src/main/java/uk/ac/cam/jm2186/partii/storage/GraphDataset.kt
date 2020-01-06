package uk.ac.cam.jm2186.partii.storage

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file.FileSourceEdge

enum class GraphDataset(val id: String) {
    SocialNetwork("social-network");

    companion object {

        private val idMap: Map<String, GraphDataset> = values().associateBy(GraphDataset::id)
        private val loadedGraphs: MutableMap<GraphDataset, Graph> = mutableMapOf()

        fun fromId(id: String) = idMap[id]

    }

    fun loadGraph() : Graph = loadedGraphs.getOrPut(this) {
        val fileSource = FileSourceEdge(false)
        val filename = "data/$id/edges.txt"
        val graph = SingleGraph(id)
        fileSource.addSink(graph)
        fileSource.readAll(filename)
        fileSource.removeSink(graph)
        return@getOrPut graph
    }
}
