package uk.ac.cam.jm2186.graffs.storage.model

import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.storage.IdClassEntity
import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(MetricExperimentId::class)
class MetricExperiment(
    @Id val metricId: MetricId,
    @Id @ManyToOne(fetch = FetchType.EAGER)
    val graph: DistortedGraph,
    val time: Long,
    val value: Double?,
    @Lob @Column(length = 2147483647)
    val graphValues: ByteArray?
) : Serializable

@IdClassEntity
data class MetricExperimentId(
    val metricId: MetricId,
    val graph: DistortedGraph
) : Serializable
