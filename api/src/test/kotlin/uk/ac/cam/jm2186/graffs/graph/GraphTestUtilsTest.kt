package uk.ac.cam.jm2186.graffs.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class GraphTestUtilsTest {

    @Test
    fun createGraphId() {
        val id = GraphTestUtils.createGraphId()
        assertEquals("uk.ac.cam.jm2186.graffs.graph.GraphTestUtilsTest.createGraphId:11", id)
    }

    @Test
    fun createGraphId_ofParent() {
        val id = createGraphId_ofParent_createId()
        assertEquals("uk.ac.cam.jm2186.graffs.graph.GraphTestUtilsTest.createGraphId_ofParent:17", id)
    }

    private fun createGraphId_ofParent_createId() = GraphTestUtils.createGraphId(1)

    @Test
    fun createGraphId_withIndex() {
        val id = GraphTestUtils.createGraphId(0, 7)
        assertEquals("uk.ac.cam.jm2186.graffs.graph.GraphTestUtilsTest.createGraphId_withIndex:25-7", id)
    }

    @Test
    fun testGenerateSmallGraph() {
        val graph = GraphTestUtils.generateSmallGraph()
        GraphAssert.assertGraphEquals(
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
        """.trimIndent(),
            graph
        )
    }

    @Test
    fun testAddRandomWeights() {
        val graph = GraphTestUtils.generateSmallGraph()
        graph.addRandomWeights()
        GraphAssert.assertGraphEquals(
            """
            0
            1 [1->0_1{w=0.22631526597231777}]
            2
            3 [3->1_3{w=0.5926209788406316}]
            4 [4->3_4{w=0.2943534810687458},4->2_4{w=0.5930778252098076}]
            5 [5->4_5{w=0.18360383903895205},5->3_5{w=0.13814851556395114},5->0_5{w=0.08548125910056925}]
            6 [6->2_6{w=0.9049568172356872}]
            7 [7->5_7{w=0.9704900678643866},7->2_7{w=0.8041839195069418},7->3_7{w=0.3657716137777556},7->4_7{w=0.3175710096879262}]
            8 [8->5_8{w=0.27072817312675046},8->0_8{w=0.6353973221914591}]
            9 [9->0_9{w=0.36413656556782126},9->3_9{w=0.4436905600928558},9->5_9{w=0.955049053082473},9->6_9{w=0.4483654485305105},9->8_9{w=0.22113002870795595}]
        """.trimIndent(), graph
        )
    }
}
