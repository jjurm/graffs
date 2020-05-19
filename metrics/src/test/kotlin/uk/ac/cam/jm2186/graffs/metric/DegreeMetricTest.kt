package uk.ac.cam.jm2186.graffs.metric

import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class DegreeMetricTest : TestOnGraph() {

    @Test
    fun evaluate() {
        DegreeMetric.evaluate(graph)
        GraphAssert.assertGraphEquals(
            """
            {Degree=true}
            0{D=4}
            1{D=2} [1->0_1]
            2{D=3}
            3{D=5} [3->1_3]
            4{D=4} [4->3_4,4->2_4]
            5{D=6} [5->4_5,5->3_5,5->0_5]
            6{D=2} [6->2_6]
            7{D=4} [7->5_7,7->2_7,7->3_7,7->4_7]
            8{D=3} [8->5_8,8->0_8]
            9{D=5} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }
}
