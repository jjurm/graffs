package uk.ac.cam.jm2186.graffs.metric

import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.TestOnGraph

internal class PageRankMetricTest : TestOnGraph() {

    @Test
    fun evaluate0() {
        PageRankMetric.evaluate(graph)
        GraphAssert.assertGraphEquals(
            """
            {PageRank=true}
            0{P=0.1055012658499259}
            1{P=0.05896999256688189} [1->0_1]
            2{P=0.08484843821073532}
            3{P=0.12676862359167393} [3->1_3]
            4{P=0.10347442961825434} [4->3_4,4->2_4]
            5{P=0.14749096157432331} [5->0_5,5->3_5,5->4_5]
            6{P=0.06087666382286494} [6->2_6]
            7{P=0.10347442961825436} [7->2_7,7->3_7,7->4_7,7->5_7]
            8{P=0.08014953327910741} [8->0_8,8->5_8]
            9{P=0.12844566186797862} [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]
        """.trimIndent(), graph
        )
    }
}
