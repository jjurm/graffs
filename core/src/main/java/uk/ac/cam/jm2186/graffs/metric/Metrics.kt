package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph

typealias MetricId = String

interface Metric {
    fun evaluate(graph: Graph): MetricResult?

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

sealed class MetricResult {
    object Unit : MetricResult() {
        override fun toString() = "[true]"
    }

    class Double(val value: kotlin.Double) : MetricResult() {
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
