package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph

typealias MetricId = String

interface Metric {
    fun evaluate(graph: Graph): MetricResult?

    fun cleanup(graph: Graph) {}

    companion object {
        val map: Map<MetricId, MetricInfo> by lazy {
            listOf<MetricInfo>(
                //AverageDegreeMetric,
                BetweennessCentralityMetric,
                ClosenessCentrality,
                //DangalchevClosenessCentralityMetric,
                DegreeMetric,
                Ego1EdgesMetric,
                Ego2NodesMetric,
                EgoRatioMetric,
                HarmonicCentrality,
                LocalClusteringMetric,
                PageRankMetric,
                RedundancyMetric
            ).map { it.id to it }.toMap()
        }
    }
}

typealias MetricFactory = () -> Metric

sealed class MetricResult(val time: Long) {
    class Unit(time: Long) : MetricResult(time) {
        override fun toString() = "[true]"
    }

    class Double(val value: kotlin.Double, time: Long) : MetricResult(time) {
        override fun toString() = "%.2f".format(value)
    }
}

interface MetricInfo {
    val id: MetricId
    val factory: MetricFactory
    val isNodeMetric: Boolean

    val dependencies: Set<MetricInfo> get() = setOf()
    val attributeName: String get() = id
}
