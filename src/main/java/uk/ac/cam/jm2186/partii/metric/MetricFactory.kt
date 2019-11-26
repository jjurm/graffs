package uk.ac.cam.jm2186.partii.metric

import org.graphstream.graph.Graph
import java.io.Serializable

interface MetricFactory<Result : Serializable> {

    fun createMetric(sourceGraph: Graph, params: List<Number>): Metric<Result>

}
