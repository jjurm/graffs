package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Graph

interface GraphProducerFactory {

    fun createGraphProducer(sourceGraph: Graph, seed: Long, params: List<Number>): GraphProducer

}
