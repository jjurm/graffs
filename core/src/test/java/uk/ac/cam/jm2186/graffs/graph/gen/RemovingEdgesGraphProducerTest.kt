package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.GraphTestUtils

internal class RemovingEdgesGraphProducerTest {

    @Test
    fun produce() {
        val graph = GraphTestUtils.generateSmallGraph()
        val producer = RemovingEdgesGenerator(
            deletionRate = 0.3,
            seed = 42
        )
        val generated = runBlocking(Dispatchers.Default) {
            producer.produce(graph, 1, this)[0].await()
        }

        GraphAssert.assertGraphEquals(
            """
            0
            1 [1->0_1]
            2
            3
            4
            5 [5->4_5,5->0_5]
            6
            7 [7->5_7,7->2_7,7->3_7,7->4_7]
            8 [8->0_8]
            9 [9->0_9,9->3_9,9->6_9]""",
            generated.graph
        )
    }
}
