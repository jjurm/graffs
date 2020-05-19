package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Node
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class EgoMetricsTest : TestOnGraph() {

    @Test
    fun expand() {
        val n = graph.getNode<Node>(1)
        assertEquals(
            setOf(1, 0, 3),
            setOf(n).expand().map { it.index }.toSet()
        )
    }

    @Test
    fun testEgo1EdgesMetric() {
        Ego1EdgesMetric.evaluate(graph)
        GraphAssert.assertGraphEquals(
            """
            {Ego1Edges=true}
            0{EE=7}
            1{EE=2} [1->0_1]
            2{EE=4}
            3{EE=9} [3->1_3]
            4{EE=8} [4->3_4,4->2_4]
            5{EE=13} [5->4_5,5->3_5,5->0_5]
            6{EE=2} [6->2_6]
            7{EE=8} [7->5_7,7->2_7,7->3_7,7->4_7]
            8{EE=6} [8->5_8,8->0_8]
            9{EE=9} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }

    @Test
    fun testEgo2NodesMetric() {
        Ego2NodesMetric.evaluate(graph)
        GraphAssert.assertGraphEquals(
            """
            {Ego2Nodes=true}
            0{E2=9}
            1{E2=8} [1->0_1]
            2{E2=7}
            3{E2=10} [3->1_3]
            4{E2=10} [4->3_4,4->2_4]
            5{E2=10} [5->4_5,5->3_5,5->0_5]
            6{E2=9} [6->2_6]
            7{E2=10} [7->5_7,7->2_7,7->3_7,7->4_7]
            8{E2=9} [8->5_8,8->0_8]
            9{E2=10} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }

    @Test
    fun testEgoRatioMetric() {
        EgoRatioMetric.dependencies.forEach { it.factory().evaluate(graph) }
        EgoRatioMetric.evaluate(graph)
        GraphAssert.assertGraphEquals(
            """
            {Ego2Nodes=true,EgoRatio=true}
            0{E2=9,ER=0.5555555555555556}
            1{E2=8,ER=0.375} [1->0_1]
            2{E2=7,ER=0.5714285714285714}
            3{E2=10,ER=0.6} [3->1_3]
            4{E2=10,ER=0.5} [4->3_4,4->2_4]
            5{E2=10,ER=0.7} [5->4_5,5->3_5,5->0_5]
            6{E2=9,ER=0.3333333333333333} [6->2_6]
            7{E2=10,ER=0.5} [7->5_7,7->2_7,7->3_7,7->4_7]
            8{E2=9,ER=0.4444444444444444} [8->5_8,8->0_8]
            9{E2=10,ER=0.6} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }
}
