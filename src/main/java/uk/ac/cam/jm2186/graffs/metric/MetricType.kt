package uk.ac.cam.jm2186.graffs.metric

import java.io.Serializable

typealias MetricId = String

interface MetricFactory : Serializable {
    fun createMetric(params: List<Number>): Metric
}

class MetricType private constructor(
    val id: MetricId,
    val metricFactory: MetricFactory
) : Serializable {
    companion object {
        private val metrics = mutableMapOf<MetricId, MetricType>()

        private fun register(
            id: MetricId,
            metricFactory: MetricFactory
        ) {
            val metricType = MetricType(id, metricFactory)
            metrics[metricType.id] = metricType
        }

        fun values(): Collection<MetricType> = metrics.values
        fun ids(): Collection<MetricId> = metrics.keys

        fun byId(id: MetricId): MetricType = metrics.getValue(id)

        init {
            register("AverageDegree", AverageDegreeMetric.Factory())
            register("BetweennessCentrality", BetweennessCentralityMetric.Factory())
            register("AverageBetweennessCentrality", AverageBetweennessCentralityMetric.Factory())
        }
    }
}
