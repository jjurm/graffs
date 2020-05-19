package uk.ac.cam.jm2186.graffs.metric

import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class LocalClusteringMetricTest : TestOnGraph() {

    @Test
    fun evaluate0() {
        LocalClusteringMetric.dependencies.forEach { it.factory().evaluate(graph) }
        LocalClusteringMetric.evaluate(graph)
        GraphAssert.assertGraphEquals(
            """
            {LocalClustering=true}
            0{LC=0.5}
            1{LC=0.0} [1->0_1]
            2{LC=0.3333333333333333}
            3{LC=0.4} [3->1_3]
            4{LC=0.6666666666666666} [4->3_4,4->2_4]
            5{LC=0.4666666666666667} [5->4_5,5->3_5,5->0_5]
            6{LC=0.0} [6->2_6]
            7{LC=0.6666666666666666} [7->5_7,7->2_7,7->3_7,7->4_7]
            8{LC=1.0} [8->5_8,8->0_8]
            9{LC=0.4} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }
}
