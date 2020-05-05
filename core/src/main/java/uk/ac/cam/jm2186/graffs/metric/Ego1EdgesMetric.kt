package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node


object Ego1EdgesMetric : SingletonMetric("Ego1Edges") {
    override val isNodeMetric get() = true

    override fun evaluate0(graph: Graph) {
        graph.getEachNode<Node>().forEach { node ->
            val edges = node.getNeighborNodeIterator<Node>().asSequence()
                .map { alter -> alter.degree }
                .sum() / 2
            node.addAttribute(attributeName, edges)
        }
    }
}

object Ego2NodesMetric : SingletonMetric("Ego2Nodes") {
    override val isNodeMetric get() = true

    override fun evaluate0(graph: Graph) {
        graph.getEachNode<Node>().forEach { node ->
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

    override fun evaluate0(graph: Graph) {
        graph.getEachNode<Node>().forEach { node ->
            val ego1nodes = node.degree
            val ego2nodes = node.getMetricValue(Ego2NodesMetric)
            val ratio = ego1nodes / ego2nodes
            node.addAttribute(attributeName, ratio)
        }
    }
}
