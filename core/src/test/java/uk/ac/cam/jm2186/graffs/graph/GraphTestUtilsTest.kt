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
            5 [5->0_5,5->3_5,5->4_5]
            6 [6->2_6]
            7 [7->2_7,7->3_7,7->4_7,7->5_7]
            8 [8->0_8,8->5_8]
            9 [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]""",
            graph
        )
    }
}
