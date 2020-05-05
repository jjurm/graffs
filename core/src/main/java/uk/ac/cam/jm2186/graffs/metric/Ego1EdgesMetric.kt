package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node


object Ego1EdgesMetric : SingletonMetric("Ego1Edges") {
    override val isNodeMetric get() = true
    override val attributeName = "EE"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val edges = node.getNeighborNodeIterator<Node>().asSequence()
                .map { alter -> alter.degree }
                .sum() / 2
            node.addAttribute(attributeName, edges)
        }
    }
}

object Ego2NodesMetric : SingletonMetric("Ego2Nodes") {
    override val isNodeMetric get() = true
    override val attributeName = "E2"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val nodes = node.getNeighborNodeIterator<Node>().asSequence()
                .flatMap { alter -> alter.getNeighborNodeIterator<Node>().asSequence() }
                .count()
            node.addAttribute(attributeName, nodes)
        }
    }
}

object EgoRatioMetric : SingletonMetric("EgoRatio") {
    override val isNodeMetric get() = true
    override val dependencies: Set<MetricInfo> = setOf(Ego2NodesMetric)
    override val attributeName = "ER"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val ego1nodes = node.degree
            val ego2nodes = node.getMetricValue(Ego2NodesMetric)
            val ratio = ego1nodes / ego2nodes
            val v = if (ratio.isNaN()) 0.0 else ratio
            node.addAttribute(attributeName, v)
        }
    }
}
