package uk.ac.cam.jm2186.partii.graph

import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.partii.GraphAssert

internal class RemovingEdgesGraphProducerTest {

    @Test
    fun produceAndCompute() {
        val graph = GraphTestUtils.generateSmallGraph()
        val producer = RemovingEdgesGraphProducer(graph, deletionRate = 0.3, seed = 42)
        val generated = producer.produce()
        producer.compute()

        GraphAssert.assertGraphEquals(
            """
            0
            1
            2
            3 [3->1_3]
            4 [4->2_4]
            5
            6 [6->2_6]
            7 [7->5_7,7->2_7,7->3_7,7->4_7]
            8 [8->0_8]
            9 [9->0_9,9->3_9,9->5_9,9->6_9]""",
            generated
        )
    }

    @Test
    fun produceComputeCombined() {
        val graph = GraphTestUtils.generateSmallGraph()
        val producer = RemovingEdgesGraphProducer(graph, deletionRate = 0.3, seed = 42)
        val generated = producer.produceComputed()

        GraphAssert.assertGraphEquals(
            """
            0
            1
            2
            3 [3->1_3]
            4 [4->3_4,4->2_4]
            5 [5->0_5,5->3_5]
            6
            7 [7->4_7,7->2_7]
            8 [8->0_8]
            9 [9->0_9,9->8_9,9->5_9,9->6_9]""",
            generated
        )
    }
}
