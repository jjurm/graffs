package uk.ac.cam.jm2186.graffs.graph.gen

import org.junit.jupiter.api.Test

internal class RemovingEdgesSequenceGeneratorTest : GraphProducerTestBase() {

    @Test
    fun produce() {
        testGraphProducer(
            { cs ->
                RemovingEdgesSequenceGenerator(
                    deletionRate = 0.3,
                    seed = 42,
                    coroutineScope = cs
                )
            },
            listOf(
                """
                0
                1 [1->0_1]
                2
                3 [3->1_3]
                4 [4->3_4,4->2_4]
                5 [5->4_5,5->3_5,5->0_5]
                6 [6->2_6]
                7 [7->5_7,7->2_7,7->3_7,7->4_7]
                8 [8->5_8,8->0_8]
                9 [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
            """.trimIndent(), """
                0
                1 [1->0_1]
                2
                3
                4
                5 [5->4_5,5->0_5]
                6
                7 [7->5_7,7->2_7,7->3_7,7->4_7]
                8 [8->0_8]
                9 [9->0_9,9->3_9,9->6_9]
            """.trimIndent()
            )
        )
    }
}
