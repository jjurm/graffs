package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.Graphs
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.GraphReplay

class IdentityGraphProducer(
    private val sourceGraph: Graph
) : GraphProducer {

    class Factory : GraphProducerFactory {
        override fun createGraphProducer(sourceGraph: Graph, seed: Long, params: List<Number>) =
            IdentityGraphProducer(sourceGraph)
    }

    private val id = sourceGraph.id + "-" + javaClass.name
    private var count = 0
    val replay = GraphReplay(id)

    override fun produce(): Graph {
        val graph = SingleGraph("$id-${count++}")
        replay.addSink(graph)
        return graph
    }

    override fun compute() {
        replay.replay(sourceGraph)
    }

    override fun produceComputed(): Graph {
        return Graphs.clone(sourceGraph)
    }
}
