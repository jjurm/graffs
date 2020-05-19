package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Node
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class ClosenessCentralityTest : TestOnGraph() {

    @Test
    fun evaluate0() {
        ClosenessCentrality.dependencies.forEach { it.factory().evaluate(graph) }
        ClosenessCentrality.evaluate(graph)
        graph.getNodeSet<Node>().toList().forEach { node ->
            node.attributeKeySet.toList().forEach { attr ->
                if (attr != ClosenessCentrality.attributeName) {
                    node.removeAttribute(attr)
                }
            }
        }
        GraphAssert.assertGraphEquals(
            """
            {Closeness=true,APSP=true}
            0{C=0.06666666666666667}
            1{C=0.05555555555555555} [1->0_1]
            2{C=0.05555555555555555}
            3{C=0.07692307692307693} [3->1_3]
            4{C=0.07142857142857142} [4->3_4,4->2_4]
            5{C=0.08333333333333333} [5->0_5,5->3_5,5->4_5]
            6{C=0.058823529411764705} [6->2_6]
            7{C=0.07142857142857142} [7->2_7,7->3_7,7->4_7,7->5_7]
            8{C=0.0625} [8->0_8,8->5_8]
            9{C=0.07692307692307693} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }
}
