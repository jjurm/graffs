package uk.ac.cam.jm2186.graffs.db.model

import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDatasetId
import uk.ac.cam.jm2186.graffs.db.IdClassEntity
import java.io.Serializable
import javax.persistence.*
import uk.ac.cam.jm2186.graffs.db.Entity as EntityIntf

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
) : EntityIntf()

@IdClassEntity
class RobustnessId(
    val experiment: Experiment,
    val dataset: GraphDatasetId,
    val metric: MetricId,
    val robustnessMeasure: RobustnessMeasureId
) : Serializable
