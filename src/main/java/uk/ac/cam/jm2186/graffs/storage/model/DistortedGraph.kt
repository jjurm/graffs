package uk.ac.cam.jm2186.graffs.storage.model

import org.graphstream.graph.Graph
import org.graphstream.stream.file.FileSinkDGS
import org.graphstream.stream.file.FileSourceDGS
import uk.ac.cam.jm2186.graffs.graph.readGraph
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Serializable
import javax.persistence.*

@Embeddable
class DistortedGraph(
    graph: Graph?
) : Serializable {

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(length = 2147483647)
    private var serialized: ByteArray? = null
    private var graphstreamId: String? = null

    var graph: Graph?
        get() = when (val s = serialized) {
            null -> null
            else -> {
                FileSourceDGS().readGraph(ByteArrayInputStream(s), graphstreamId!!)
            }
        }
        set(value) = when(value) {
            null -> Unit
            else -> {
                val out = ByteArrayOutputStream()
                FileSinkDGS().writeAll(value, out)
                serialized = out.toByteArray()
                graphstreamId = value.id
            }
        }

    init {
        this.graph = graph
    }

}
