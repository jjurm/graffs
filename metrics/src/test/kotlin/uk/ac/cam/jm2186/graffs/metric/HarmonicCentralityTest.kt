package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Node
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class HarmonicCentralityTest : TestOnGraph() {

    @Test
    fun evaluate0() {
        HarmonicCentrality.dependencies.forEach { it.factory().evaluate(graph) }
        HarmonicCentrality.evaluate(graph)
        graph.getNodeSet<Node>().toList().forEach { node ->
            node.attributeKeySet.toList().forEach { attr ->
                if (attr != HarmonicCentrality.attributeName) {
                    node.removeAttribute(attr)
                }
            }
        }
        GraphAssert.assertGraphEquals(
            """
            {Harmonic=true,APSP=true}
            0{H=6.333333333333333}
            1{H=5.166666666666666} [1->0_1]
            2{H=5.499999999999999}
            3{H=7.0} [3->1_3]
            4{H=6.5} [4->3_4,4->2_4]
            5{H=7.5} [5->4_5,5->3_5,5->0_5]
            6{H=5.333333333333333} [6->2_6]
            7{H=6.5} [7->5_7,7->2_7,7->3_7,7->4_7]
            8{H=5.833333333333333} [8->5_8,8->0_8]
            9{H=7.0} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }
}
