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
    val graph: GeneratedGraph,
    val value: Double?,
    @Lob @Column(length = 2147483647)
    val graphValues: ByteArray?
) : Serializable

@IdClassEntity
class MetricExperimentId(
    val metricId: MetricId,
    val graph: GeneratedGraph
) : Serializable {
    operator fun component1() = metricId
    operator fun component2() = graph
}
