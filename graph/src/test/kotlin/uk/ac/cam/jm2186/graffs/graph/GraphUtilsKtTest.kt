package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.ui.layout.springbox.implementations.SpringBox
import org.graphstream.util.Filters
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import uk.ac.cam.jm2186.graffs.graph.storage.stringRepresentation
import java.util.*

internal class GraphUtilsKtTest : TestOnGraph() {

    @Test
    fun testCopy() {
        val copy = graph.copy()
        assertFalse(copy === graph)
        GraphAssert.assertGraphEquals(graph.stringRepresentation(), copy)
    }

    @Test
    fun testHasWeights() {
        assertFalse(graph.hasWeights())
        graph.getEdgeSet<Edge>().forEach { edge ->
            edge.weight = 1.0
        }
        assertTrue(graph.hasWeights())
    }

    @Test
    fun testSubgraph() {
        val nodes = listOf("0", "1", "2", "3", "4").map { graph.getNode<Node>(it) }.toSet()
        val edges = listOf("1_3", "2_4").map { graph.getEdge<Edge>(it) }.toSet()
        val exp = """
            0
            1
            2
            3 [3->1_3]
            4 [4->2_4]
        """.trimIndent()

        val res1 = graph.subgraph(nodeSet = nodes, edgeSet = edges, id = graph.id + "1")
        GraphAssert.assertGraphEquals(exp, res1)

        val res2 = graph.subgraph(
            nodeFilter = Filters.isContained(nodes),
            edgeFilter = Filters.isContained(edges),
            id = graph.id + "1"
        )
        GraphAssert.assertGraphEquals(exp, res2)
    }

    @Test
    fun testFilterAtThreshold() {
        graph.addRandomWeights()
        GraphAssert.assertGraphEquals(
            """
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
        """.trimIndent(), graph.filterAtThreshold(0.5, "x", 1)
        )
    }

    @Test
    fun getNumberAttribute() {
        assertThrows(IllegalStateException::class.java) { graph.getNumberAttribute("x") }
        graph.addAttribute("x", 1.0)
        assertEquals(1.0, graph.getNumberAttribute("x"))
    }

    @Test
    fun appendAttribute() {
        graph.appendAttribute("x", "y")
        assertEquals("y", graph.getAttribute("x"))
        graph.appendAttribute("x", 1.0)
        assertArrayEquals(arrayOf("y", 1.0), graph.getAttribute("x"))
    }

    @Test
    fun computeLayout() {
        val layout = SpringBox(false, Random(42))
        graph.computeLayout(layout, 0.95)
        graph.getEachNode<Node>().forEach {
            assertTrue(it.hasAttribute("xyz"))
        }
    }
}
