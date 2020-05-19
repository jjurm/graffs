package uk.ac.cam.jm2186.graffs.db.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.gen.GraphProducers

fun GraphGenerator.produceFromGraph(
    sourceGraph: Graph,
    coroutineScope: CoroutineScope
): List<Deferred<PerturbedGraph>> {
    val generatorFactory = GraphProducers.map.getValue(method)
    return generatorFactory(seed, params, coroutineScope)
        .produce(sourceGraph, n)
}
