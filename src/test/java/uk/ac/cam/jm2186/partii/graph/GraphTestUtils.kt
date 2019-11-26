package uk.ac.cam.jm2186.partii.graph

import org.graphstream.algorithm.generator.RandomGenerator
import org.graphstream.graph.implementations.DefaultGraph

object GraphTestUtils {

    /**
     * Returns a string that can be used as an ID of a generated Graph, satisfying the following
     * - each location in code gets unique ID
     * - IDs are constant across program runs, to prevent randomness in tests
     *
     * @param [callers] 0 means the invoking method will be used, and any _n > 0_ means the stack trace will look _n_ levels
     * above the caller of this [createGraphId].
     * @param [index] allows this method to be called in a loop (therefore having the same location in code) and still
     * produce unique IDs
     */
    fun createGraphId(callers: Int, index: Int?): String {
        val stackTraceElement = Thread.currentThread().stackTrace[2 + callers]
        val suffix = if (index == null) "" else "-$index"
        return "${stackTraceElement.className}.${stackTraceElement.methodName}:${stackTraceElement.lineNumber}$suffix"
    }

    /**
     * Returns a string that can be used as an ID of a generated Graph, satisfying the following
     * - each location in code gets unique ID
     * - IDs are constant across program runs, to prevent randomness in tests
     *
     * @param [callers] 0 means the invoking method will be used, and any _n > 0_ means the stack trace will look _n_ levels
     * above the caller of this [createGraphId].
     */
    fun createGraphId(callers: Int): String {
        val stackTraceElement = Thread.currentThread().stackTrace[2 + callers]
        return "${stackTraceElement.className}.${stackTraceElement.methodName}:${stackTraceElement.lineNumber}"
    }

    /**
     * Returns a string that can be used as an ID of a generated Graph, satisfying the following
     * - each location in code gets unique ID
     * - IDs are constant across program runs, to prevent randomness in tests
     */
    fun createGraphId(): String {
        val stackTraceElement = Thread.currentThread().stackTrace[2]
        return "${stackTraceElement.className}.${stackTraceElement.methodName}:${stackTraceElement.lineNumber}"
    }

    /**
     * Generates a small graph. This function always returns a graph of the same structure (but the ID is generated)
     */
    fun generateSmallGraph(): DefaultGraph {
        val graph = DefaultGraph(createGraphId(1))
        val generator = RandomGenerator(4.0, true, false)
        generator.setRandomSeed(42)
        generator.addSink(graph)
        generator.begin()
        while (graph.nodeCount < 10) generator.nextEvents()
        generator.end()
        return graph
    }

}
