package uk.ac.cam.jm2186.graffs.storage.model

import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.storage.GraphDatasetId
import uk.ac.cam.jm2186.graffs.storage.IdClassEntity
import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(RobustnessId::class)
class Robustness(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment")
    @Id
    val experiment: Experiment,
    @Id
    val dataset: GraphDatasetId,
    @Id
    val metric: MetricId,
    @Id
    val robustnessMeasure: RobustnessMeasureId,

    val value: Double
) : Serializable

@IdClassEntity
class RobustnessId(
    val experiment: Experiment,
    val dataset: GraphDatasetId,
    val metric: MetricId,
    val robustnessMeasure: RobustnessMeasureId
) : Serializable
