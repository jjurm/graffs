package uk.ac.cam.jm2186.partii.graph

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import uk.ac.cam.jm2186.partii.GraphAssert

internal class GraphTestUtilsTest {

    @Test
    fun createGraphId() {
        val id = GraphTestUtils.createGraphId()
        assertEquals("uk.ac.cam.jm2186.partii.graph.GraphTestUtilsTest.createGraphId:12", id)
    }

    @Test
    fun createGraphId_ofParent() {
        val id = createGraphId_ofParent_createId()
        assertEquals("uk.ac.cam.jm2186.partii.graph.GraphTestUtilsTest.createGraphId_ofParent:18", id)
    }

    private fun createGraphId_ofParent_createId() = GraphTestUtils.createGraphId(1)

    @Test
    fun createGraphId_withIndex() {
        val id = GraphTestUtils.createGraphId(0, 7)
        assertEquals("uk.ac.cam.jm2186.partii.graph.GraphTestUtilsTest.createGraphId_withIndex:26-7", id)
    }

    @Test
    fun testGenerateSmallGraph() {
        val graph = GraphTestUtils.generateSmallGraph()
        GraphAssert.assertGraphEquals(
            """
            DGS004
            null 0 0
            an "0"
            an "1"
            an "2"
            an "3"
            an "4"
            an "5"
            an "6"
            an "7"
            an "8"
            an "9"
            ae "0_1" "0"  "1"
            ae "2_6" "2"  "6"
            ae "5_8" "5"  "8"
            ae "5_7" "5"  "7"
            ae "4_5" "4"  "5"
            ae "1_3" "1"  "3"
            ae "3_4" "3"  "4"
            ae "3_5" "3"  "5"
            ae "2_4" "2"  "4"
            ae "0_5" "0"  "5"
            ae "0_8" "0"  "8"
            ae "2_7" "2"  "7"
            ae "3_7" "3"  "7"
            ae "4_7" "4"  "7"
            ae "0_9" "0"  "9"
            ae "3_9" "3"  "9"
            ae "5_9" "5"  "9"
            ae "6_9" "6"  "9"
            ae "8_9" "8"  "9"
        """, graph
        )
    }
}
