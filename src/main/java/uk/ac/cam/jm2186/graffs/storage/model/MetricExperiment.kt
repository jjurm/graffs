package uk.ac.cam.jm2186.graffs.storage.model

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file.FileSinkDGS
import org.graphstream.stream.file.FileSourceDGS
import uk.ac.cam.jm2186.graffs.graph.readGraph
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.storage.IdClassEntity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(MetricExperimentId::class)
class MetricExperiment(
    @Id val metricId: MetricId,
    @Id @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(nullable = false)
    val graph: DistortedGraphOld,

    val time: Long,
    val value: Double?
) : Serializable {

    @Lob @Column(length = 2147483647)
    var graphValues: ByteArray? = null

    fun readGraphValues(): SingleGraph {
        val id = "MetricExperiment-${metricId}-${graph.getId()}"
        return FileSourceDGS().readGraph(ByteArrayInputStream(graphValues), id)
    }

    fun writeGraphValues(graph: Graph?) {
        if (graph == null) {
            graphValues = null
        } else {
            val out = ByteArrayOutputStream()
            FileSinkDGS().writeAll(graph, out)
            graphValues = out.toByteArray()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetricExperiment) return false

        if (metricId != other.metricId) return false
        if (graph != other.graph) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metricId.hashCode()
        result = 31 * result + graph.hashCode()
        return result
    }

}

@IdClassEntity
data class MetricExperimentId(
    val metricId: MetricId,
    val graph: DistortedGraphOld
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetricExperimentId) return false

        if (metricId != other.metricId) return false
        if (graph != other.graph) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metricId.hashCode()
        result = 31 * result + graph.hashCode()
        return result
    }
}
