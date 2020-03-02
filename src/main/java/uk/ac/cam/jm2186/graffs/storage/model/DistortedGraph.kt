package uk.ac.cam.jm2186.graffs.storage.model

import org.graphstream.graph.Graph
import org.graphstream.stream.file.FileSinkDGS
import org.graphstream.stream.file.FileSourceDGS
import uk.ac.cam.jm2186.graffs.graph.readGraph
import uk.ac.cam.jm2186.graffs.storage.AbstractJpaPersistable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.persistence.*

@Embeddable
class DistortedGraph(
    graph: Graph?
) : AbstractJpaPersistable<Long>() {

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(length = 2147483647)
    private var serialized: ByteArray? = null

    var graph: Graph?
        get() = when (val s = serialized) {
            null -> null
            else -> {
                val id = "DistortedGraph-${getId()}"
                FileSourceDGS().readGraph(ByteArrayInputStream(s), id)
            }
        }
        set(value) = when(value) {
            null -> Unit
            else -> {
                val out = ByteArrayOutputStream()
                FileSinkDGS().writeAll(value, out)
                serialized = out.toByteArray()
            }
        }

    init {
        this.graph = graph
    }

}
