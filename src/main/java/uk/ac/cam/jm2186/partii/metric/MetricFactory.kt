package uk.ac.cam.jm2186.partii.metric

import java.io.Serializable

interface MetricFactory<Result : Serializable> : Serializable {

    fun createMetric(params: List<Number>): Metric<Result>

}
