package uk.ac.cam.jm2186.partii.metric

import java.io.Serializable

typealias MetricId = String

class MetricType<Result : Serializable> private constructor(
    val id: MetricId,
    val metricFactory: MetricFactory<Result>,
    val resultClass: Class<Result>
) : Serializable {
    companion object {
        private val metrics = mutableMapOf<MetricId, MetricType<*>>()

        private fun register(metricType: MetricType<*>) {
            metrics[metricType.id] = metricType
        }

        fun values(): Collection<MetricType<*>> = metrics.values
        fun ids(): Collection<MetricId> = metrics.keys

        init {
            register(MetricType("AverageDegree", AverageDegreeMetric.Factory(), Double::class.java))
        }
    }
}
