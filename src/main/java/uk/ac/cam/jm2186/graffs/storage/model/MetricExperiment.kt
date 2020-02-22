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
    val graph: DistortedGraph,
    val time: Long,
    val value: Double?,
    @Lob @Column(length = 2147483647)
    val graphValues: ByteArray?
) : Serializable {

    fun readValuesGraph(): SingleGraph {
        val id = "MetricExperiment-${metricId}-${graph.getId()}"
        return FileSourceDGS().readGraph(ByteArrayInputStream(graphValues), id)
    }

    companion object {
        fun writeValuesGraph(graph: Graph): ByteArray {
            val out = ByteArrayOutputStream()
            FileSinkDGS().writeAll(graph, out)
            return out.toByteArray()
        }
    }

}

@IdClassEntity
data class MetricExperimentId(
    val metricId: MetricId,
    val graph: DistortedGraph
) : Serializable
