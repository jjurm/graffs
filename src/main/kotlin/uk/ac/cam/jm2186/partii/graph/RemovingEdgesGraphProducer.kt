package uk.ac.cam.jm2186.partii.graph

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.Graphs
import org.graphstream.graph.implementations.SingleGraph
import java.util.*
import kotlin.random.Random

class RemovingEdgesGraphProducer(
    private val sourceGraph: SingleGraph,
    private val deletionRate: Double = 0.05,
    seed: Long = 42L
) : GraphProducer {

    private val random = Random(seed)

    override fun produce(): Graph {
        val g = Graphs.clone(sourceGraph)
        g.getEachEdge<Edge>().removeAll { random.nextDouble() < deletionRate }
        return g
    }

}
