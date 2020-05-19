package uk.ac.cam.jm2186.graffs.metric

import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert.assertGraphEquals
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class BetweennessCentralityMetricTest : TestOnGraph() {

    @Test
    fun evaluate0() {
        BetweennessCentralityMetric.evaluate(graph)
        assertGraphEquals(
            """
            {Betweenness=true}
            0{B=5.0}
            1{B=0.6666666666666666} [1->0_1]
            2{B=4.0}
            3{B=11.0} [3->1_3]
            4{B=4.333333333333333} [4->3_4,4->2_4]
            5{B=14.333333333333332} [5->4_5,5->3_5,5->0_5]
            6{B=3.333333333333333} [6->2_6]
            7{B=4.333333333333333} [7->5_7,7->2_7,7->3_7,7->4_7]
            8{B=0.0} [8->5_8,8->0_8]
            9{B=13.0} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }
}
