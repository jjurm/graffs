package uk.ac.cam.jm2186.graffs.storage.model

import org.graphstream.graph.Graph
import org.graphstream.stream.file.FileSinkDGS
import org.graphstream.stream.file.FileSourceDGS
import uk.ac.cam.jm2186.graffs.graph.readGraph
import uk.ac.cam.jm2186.graffs.storage.AbstractJpaPersistable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.persistence.*

@Entity
class DistortedGraph(
    val seed: Long,
    graph: Graph
) : AbstractJpaPersistable<Long>() {

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(length = 2147483647)
    private lateinit var serialized: ByteArray
    private lateinit var graphstreamId: String

    init {
        serialize(graph)
    }

    fun deserialize() = FileSourceDGS().readGraph(ByteArrayInputStream(serialized), graphstreamId)
    fun serialize(graph: Graph) {
        val out = ByteArrayOutputStream()
        FileSinkDGS().writeAll(graph, out)
        serialized = out.toByteArray()
        graphstreamId = graph.id
    }

    fun getShortHash(): String = (seed and 0xffff).toString(16)

}
