package uk.ac.cam.jm2186.graffs.metric

object Metrics {
    val map: Map<MetricId, MetricInfo> by lazy {
        listOf<MetricInfo>(
            BetweennessCentralityMetric,
            ClosenessCentrality,
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
