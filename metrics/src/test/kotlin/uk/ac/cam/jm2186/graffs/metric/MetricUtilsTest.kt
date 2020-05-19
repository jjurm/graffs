package uk.ac.cam.jm2186.graffs.metric

import kotlinx.coroutines.runBlocking
import org.graphstream.graph.Node
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class MetricUtilsTest : TestOnGraph() {

    @Test
    fun testTopologicalOrderWithDependencies() {
        assertEquals(listOf(DegreeMetric), listOf(DegreeMetric).topologicalOrderWithDependencies())
        assertEquals(
            listOf(APSPMetric, ClosenessCentrality, Ego2NodesMetric, EgoRatioMetric),
            listOf(ClosenessCentrality, EgoRatioMetric).topologicalOrderWithDependencies()
        )
    }

    @Test
    fun testEvaluateMetricsAsync() {
        val metric = EgoRatioMetric
        @Suppress("DeferredResultUnused")
        runBlocking {
            evaluateMetricsAsync(listOf(metric), { graph }).await()
        }
        graph.getNodeSet<Node>().toList().forEach { node ->
            node.attributeKeySet.toList().forEach { attr ->
                if (attr != metric.attributeName) {
                    node.removeAttribute(attr)
                }
            }
        }
        GraphAssert.assertGraphEquals(
            """
            {Ego2Nodes=true,EgoRatio=true}
            0{ER=0.5555555555555556}
            1{ER=0.375} [1->0_1]
            2{ER=0.5714285714285714}
            3{ER=0.6} [3->1_3]
            4{ER=0.5} [4->3_4,4->2_4]
            5{ER=0.7} [5->0_5,5->3_5,5->4_5]
            6{ER=0.3333333333333333} [6->2_6]
            7{ER=0.5} [7->2_7,7->3_7,7->4_7,7->5_7]
            8{ER=0.4444444444444444} [8->0_8,8->5_8]
            9{ER=0.6} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }

    @Test
    fun testEvaluateSingle() {
        val metric = EgoRatioMetric
        metric.evaluateSingle(graph)
        graph.getNodeSet<Node>().toList().forEach { node ->
            node.attributeKeySet.toList().forEach { attr ->
                if (attr != metric.attributeName) {
                    node.removeAttribute(attr)
                }
            }
        }
        GraphAssert.assertGraphEquals(
            """
            {Ego2Nodes=true,EgoRatio=true}
            0{ER=0.5555555555555556}
            1{ER=0.375} [1->0_1]
            2{ER=0.5714285714285714}
            3{ER=0.6} [3->1_3]
            4{ER=0.5} [4->3_4,4->2_4]
            5{ER=0.7} [5->0_5,5->3_5,5->4_5]
            6{ER=0.3333333333333333} [6->2_6]
            7{ER=0.5} [7->2_7,7->3_7,7->4_7,7->5_7]
            8{ER=0.4444444444444444} [8->0_8,8->5_8]
            9{ER=0.6} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }

    @Test
    fun getMetricValue() {
    }
}
