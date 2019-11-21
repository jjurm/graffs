package uk.ac.cam.jm2186.partii.graph

import org.graphstream.algorithm.generator.RandomGenerator
import org.graphstream.graph.implementations.DefaultGraph

object Utils {

    fun generateSmallGraph(): DefaultGraph {
        val graph = DefaultGraph(graphId())
        val generator = RandomGenerator(4.0, true, false)
        generator.setRandomSeed(42)
        generator.addSink(graph)
        generator.begin()
        while (graph.nodeCount < 10) generator.nextEvents()
        generator.end()
        return graph
    }

    /**
     * Returns a string that can be used as an ID of a generated Graph, satisfying the following
     * - each location in code gets unique ID
     * - IDs are constant across program runs, to prevent randomness in tests
     */
    private fun graphId(): String {
        val stackTraceElement = Thread.currentThread().stackTrace[3]
        return "${stackTraceElement.className}.${stackTraceElement.methodName}:${stackTraceElement.lineNumber}"
    }

}
