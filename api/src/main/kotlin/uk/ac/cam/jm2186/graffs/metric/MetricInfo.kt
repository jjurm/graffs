package uk.ac.cam.jm2186.graffs.metric

interface MetricInfo {
    val id: MetricId
    val factory: MetricFactory
    val isNodeMetric: Boolean

    val dependencies: Set<MetricInfo> get() = setOf()
    val attributeName: String get() = id
}
