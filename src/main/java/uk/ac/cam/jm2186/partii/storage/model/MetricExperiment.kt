package uk.ac.cam.jm2186.partii.storage.model

import uk.ac.cam.jm2186.partii.metric.MetricId
import uk.ac.cam.jm2186.partii.storage.IdClassEntity
import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(MetricExperimentId::class)
class MetricExperiment(
    @Id val metricId: MetricId,
    @Id @ManyToOne(fetch = FetchType.EAGER)
    val graph: GeneratedGraph,
    val value: Double
) : Serializable

@IdClassEntity
class MetricExperimentId(
    val metricId: MetricId,
    val graph: GeneratedGraph
) : Serializable {
    operator fun component1() = metricId
    operator fun component2() = graph
}
