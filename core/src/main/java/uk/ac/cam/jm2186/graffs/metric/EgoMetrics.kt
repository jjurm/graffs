package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node

private fun MutableSet<Node>.expand(): MutableSet<Node> {
    addAll(asSequence().flatMap { node -> node.getNeighborNodeIterator<Node>().asSequence() })
    return this
}

object Ego1EdgesMetric : SingletonMetric("Ego1Edges") {
    override val isNodeMetric get() = true
    override val attributeName = "EE"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val ego1nodes = mutableSetOf(node).expand()
            val edges = ego1nodes.sumBy { alter ->
                alter.getNeighborNodeIterator<Node>().asSequence().count { neighbour ->
                    neighbour in ego1nodes
                }
            } / 2
            node.addAttribute(attributeName, edges)
        }
    }
}

object Ego2NodesMetric : SingletonMetric("Ego2Nodes") {
    override val isNodeMetric get() = true
    override val attributeName = "E2"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val ego2nodes = mutableSetOf(node).expand().expand().size
            node.addAttribute(attributeName, ego2nodes)
        }
    }
}

object EgoRatioMetric : SingletonMetric("EgoRatio") {
    override val isNodeMetric get() = true
    override val dependencies: Set<MetricInfo> = setOf(Ego2NodesMetric)
    override val attributeName = "ER"

    override fun evaluate0(graph: Graph) {
        graph.forEach { node ->
            val ego1nodes = node.degree + 1
            val ego2nodes = node.getMetricValue(Ego2NodesMetric)
            val ratio = ego1nodes / ego2nodes
            val v = if (ratio.isNaN()) 0.0 else ratio
            node.addAttribute(attributeName, v)
        }
    }
}
