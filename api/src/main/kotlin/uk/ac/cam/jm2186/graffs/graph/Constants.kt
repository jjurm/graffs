package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node

const val GRAPH_ATTRIBUTE_EDGE_THRESHOLD = "edgeThreshold"
const val EDGE_ATTRIBUTE_WEIGHT = "w"

var Graph.threshold: Double
    get() = getNumber(GRAPH_ATTRIBUTE_EDGE_THRESHOLD).takeUnless { it.isNaN() }
        ?: throw IllegalStateException("Graph `$id` does not have any threshold stored")
    set(threshold) = setAttribute(GRAPH_ATTRIBUTE_EDGE_THRESHOLD, threshold)

val Edge.weight: Double
    get() = getNumber(EDGE_ATTRIBUTE_WEIGHT).takeUnless { it.isNaN() }
        ?: throw IllegalStateException("Edge `$id` of graph `${getNode0<Node>().graph}` has no weight")
