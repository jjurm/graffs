package uk.ac.cam.jm2186.graffs.graph.gen

import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.addRandomWeights

internal class LinearEdgeThresholdGraphProducerTest : GraphProducerTestBase() {
    @Test
    fun produce() {
        graph.addRandomWeights()
        testGraphProducer(
            { cs ->
                LinearEdgeThresholdGraphProducer(
                    coroutineScope = cs,
                    lowThreshold = 0.2,
                    highThreshold = 0.8
                )
            },
            listOf(
                """
                {edgeThreshold=0.2}
                0
                1 [1->0_1{w=0.22631526597231777}]
                2
                3 [3->1_3{w=0.5926209788406316}]
                4 [4->3_4{w=0.2943534810687458},4->2_4{w=0.5930778252098076}]
                5
                6 [6->2_6{w=0.9049568172356872}]
                7 [7->5_7{w=0.9704900678643866},7->2_7{w=0.8041839195069418},7->3_7{w=0.3657716137777556},7->4_7{w=0.3175710096879262}]
                8 [8->5_8{w=0.27072817312675046},8->0_8{w=0.6353973221914591}]
                9 [9->0_9{w=0.36413656556782126},9->3_9{w=0.4436905600928558},9->5_9{w=0.955049053082473},9->6_9{w=0.4483654485305105},9->8_9{w=0.22113002870795595}]
            """.trimIndent(), """
                {edgeThreshold=0.5}
                0
                1
                2
                3 [3->1_3{w=0.5926209788406316}]
                4 [4->2_4{w=0.5930778252098076}]
                5
                6 [6->2_6{w=0.9049568172356872}]
                7 [7->5_7{w=0.9704900678643866},7->2_7{w=0.8041839195069418}]
                8 [8->0_8{w=0.6353973221914591}]
                9 [9->5_9{w=0.955049053082473}]
            """.trimIndent(), """
                {edgeThreshold=0.8}
                0
                1
                2
                3
                4
                5
                6 [6->2_6{w=0.9049568172356872}]
                7 [7->5_7{w=0.9704900678643866},7->2_7{w=0.8041839195069418}]
                8
                9 [9->5_9{w=0.955049053082473}]
            """.trimIndent()
            )
        )
    }
}
