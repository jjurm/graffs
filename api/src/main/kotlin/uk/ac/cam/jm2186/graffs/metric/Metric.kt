package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph

interface Metric {
    fun evaluate(graph: Graph): MetricResult?

    fun cleanup(graph: Graph) {}
}
