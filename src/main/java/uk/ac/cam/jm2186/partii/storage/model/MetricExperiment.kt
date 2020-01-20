package uk.ac.cam.jm2186.partii.storage.model

import uk.ac.cam.jm2186.partii.metric.MetricId
import uk.ac.cam.jm2186.partii.storage.AbstractJpaPersistable
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne

@Entity
class MetricExperiment(
    val metricId: MetricId,
    @ManyToOne(fetch = FetchType.EAGER)
    val graph: GeneratedGraph,
    val value: Double
) : AbstractJpaPersistable<Long>()
