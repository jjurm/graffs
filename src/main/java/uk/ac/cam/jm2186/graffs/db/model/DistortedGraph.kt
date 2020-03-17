package uk.ac.cam.jm2186.graffs.db.model

import org.graphstream.graph.Graph
import org.graphstream.stream.file.FileSinkDGS
import org.graphstream.stream.file.FileSourceDGS
import uk.ac.cam.jm2186.graffs.graph.readGraph
import uk.ac.cam.jm2186.graffs.db.AbstractJpaPersistable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.persistence.*

@Entity
class DistortedGraph(
    val hash: Long,
    graph: Graph
) : AbstractJpaPersistable<Long>() {

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(length = 2147483647)
    private lateinit var serialized: ByteArray
    private lateinit var graphstreamId: String

    @Transient
    @kotlin.jvm.Transient
    private var _graph: Graph? = null

    /** Access the deserialized version of the distorted graph (with underlying cache) */
    var graph: Graph
        get() = when (val cached = _graph) {
            null -> {
                val deserialized = deserialize()
                _graph = deserialized
                deserialized
            }
            else -> cached
        }
        set(graph) {
            _graph = graph
            serialize(graph)
        }

    init {
        this.graph = graph
    }

    private fun deserialize() = FileSourceDGS().readGraph(ByteArrayInputStream(serialized), graphstreamId)
    private fun serialize(graph: Graph) {
        val out = ByteArrayOutputStream()
        FileSinkDGS().writeAll(graph, out)
        serialized = out.toByteArray()
        graphstreamId = graph.id
    }

    fun getShortHash(): String {
        val v1 = hash.hashCode()
        val v2 = (v1 and 0xffff) xor ((v1 shr 32) and 0xffff)
        return v2.toString(16)
    }

}
