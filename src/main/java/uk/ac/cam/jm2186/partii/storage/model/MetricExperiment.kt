package uk.ac.cam.jm2186.partii.storage.model

import uk.ac.cam.jm2186.partii.metric.MetricFactory
import uk.ac.cam.jm2186.partii.storage.AbstractJpaPersistable
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne

typealias MetricType = Class<out MetricFactory<*>>

@Entity
class MetricExperiment(
    val metric: MetricType,
    @ManyToOne(fetch = FetchType.EAGER)
    val graph: GeneratedGraph,
    val value: Double
) : AbstractJpaPersistable<Long>()
