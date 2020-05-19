package uk.ac.cam.jm2186.graffs.graph.storage

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert
import uk.ac.cam.jm2186.graffs.graph.readGraph

internal class FileSourceEdgeOptionalWeightTest {
    @Test
    fun testUnscored() {
        val input = """
            0 1
            0 2
            0 3
            0 4
            0 5
            0 6
            0 7
            1 2
            1 4
            1 5
            1 8
            1 9
            1 10
            3 5
        """.trimIndent() + "\n"
        val g = FileSourceEdgeOptionalWeight(false).readGraph(input.byteInputStream(), "x")
        GraphAssert.assertGraphEquals(
            """
            0
            1 [1->0]
            2 [2->1,2->7]
            3 [3->2]
            4 [4->3,4->8]
            5 [5->4,5->9,5->13]
            6 [6->5]
            7 [7->6]
            8 [8->10]
            9 [9->11]
            10 [10->12]
        """.trimIndent(), g
        )
    }

    @Test
    fun testUnscoredWithComments() {
        val input = """
            # Directed graph (each unordered pair of nodes is saved once): Cit-HepTh.txt
            # Paper citation network of Arxiv High Energy Physics Theory category
            # Nodes: 27770 Edges: 352807
            # FromNodeId ToNodeId
            1001 9304045
            1001 9308122
            1001 9309097
            1001 9311042
            1001 9401139
            1001 9404151
        """.trimIndent() + "\n"
        val g = FileSourceEdgeOptionalWeight(false).readGraph(input.byteInputStream(), "x")
        GraphAssert.assertGraphEquals(
            """
            1001
            9304045 [9304045->0]
            9308122 [9308122->1]
            9309097 [9309097->2]
            9311042 [9311042->3]
            9401139 [9401139->4]
            9404151 [9404151->5]
        """.trimIndent(), g
        )
    }

    @Test
    fun testProteinScored() {
        val input = """
            protein1 protein2 combined_score
            362663.ECP_0001 362663.ECP_0006 518
            362663.ECP_0001 362663.ECP_0003 606
            362663.ECP_0001 362663.ECP_0002 606
            362663.ECP_0001 362663.ECP_0005 518
            362663.ECP_0001 362663.ECP_0004 606
            362663.ECP_0002 362663.ECP_0798 158
            362663.ECP_0002 362663.ECP_1284 175
            362663.ECP_0002 362663.ECP_1357 170
            362663.ECP_0002 362663.ECP_0918 621
            362663.ECP_0002 362663.ECP_1773 175
            362663.ECP_0002 362663.ECP_2824 342
            362663.ECP_0002 362663.ECP_4415 320
            362663.ECP_0002 362663.ECP_2562 526
        """.trimIndent() + "\n"
        val g = FileSourceEdgeOptionalWeight(false).readGraph(input.byteInputStream(), "x")
        GraphAssert.assertGraphEquals(
            """
            362663.ECP_0001
            362663.ECP_0006 [362663.ECP_0006->0{w=518.0}]
            362663.ECP_0003 [362663.ECP_0003->1{w=606.0}]
            362663.ECP_0002 [362663.ECP_0002->2{w=606.0}]
            362663.ECP_0005 [362663.ECP_0005->3{w=518.0}]
            362663.ECP_0004 [362663.ECP_0004->4{w=606.0}]
            362663.ECP_0798 [362663.ECP_0798->5{w=158.0}]
            362663.ECP_1284 [362663.ECP_1284->6{w=175.0}]
            362663.ECP_1357 [362663.ECP_1357->7{w=170.0}]
            362663.ECP_0918 [362663.ECP_0918->8{w=621.0}]
            362663.ECP_1773 [362663.ECP_1773->9{w=175.0}]
            362663.ECP_2824 [362663.ECP_2824->10{w=342.0}]
            362663.ECP_4415 [362663.ECP_4415->11{w=320.0}]
            362663.ECP_2562 [362663.ECP_2562->12{w=526.0}]
        """.trimIndent(), g
        )
    }
}
