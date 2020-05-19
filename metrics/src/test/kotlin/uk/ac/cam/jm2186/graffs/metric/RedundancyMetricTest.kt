package uk.ac.cam.jm2186.graffs.metric

import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class RedundancyMetricTest : TestOnGraph() {

    @Test
    fun evaluate0() {
        RedundancyMetric.dependencies.forEach { it.factory().evaluate(graph) }
        RedundancyMetric.evaluate(graph)
        GraphAssert.assertGraphEquals(
            """
            {Redundancy=true,LocalClustering=true}
            0{R=1.5,LC=0.5}
            1{R=0.0,LC=0.0} [1->0_1]
            2{R=0.6666666666666666,LC=0.3333333333333333}
            3{R=1.6,LC=0.4} [3->1_3]
            4{R=2.0,LC=0.6666666666666666} [4->3_4,4->2_4]
            5{R=2.3333333333333335,LC=0.4666666666666667} [5->0_5,5->3_5,5->4_5]
            6{R=0.0,LC=0.0} [6->2_6]
            7{R=2.0,LC=0.6666666666666666} [7->2_7,7->3_7,7->4_7,7->5_7]
            8{R=2.0,LC=1.0} [8->0_8,8->5_8]
            9{R=1.6,LC=0.4} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }
}
