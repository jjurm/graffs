package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import uk.ac.cam.jm2186.graffs.graph.GraphAssert.assertGraphEquals
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

open class GraphProducerTestBase : TestOnGraph() {
    fun testGraphProducer(getProducer: (CoroutineScope) -> GraphProducer, results: List<String>) {
        runBlocking {
            val producer = getProducer(this)
            val generated = producer.produce(graph, results.size).awaitAll()
            assertEquals(results.size, generated.size)
            (results zip generated).forEachIndexed { i, (expected, g) ->
                assertEquals(i, g.index)
                assertGraphEquals(expected, g.graph)
            }
        }
    }
}
