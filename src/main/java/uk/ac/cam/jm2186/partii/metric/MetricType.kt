package uk.ac.cam.jm2186.partii.metric

import java.io.Serializable

class MetricType<Result : Serializable> private constructor(
    val id: String,
    val metricFactory: MetricFactory<Result>,
    val resultClass: Class<Result>
) : Serializable {
    companion object {
        private val metrics = mutableMapOf<String, MetricType<*>>()

        private fun register(metricType: MetricType<*>) {
            metrics[metricType.id] = metricType
        }

        fun values(): Collection<MetricType<*>> = metrics.values

        init {
            register(MetricType("AverageDegree", AverageDegreeMetric.Factory(), Double::class.java))
        }
    }
}
