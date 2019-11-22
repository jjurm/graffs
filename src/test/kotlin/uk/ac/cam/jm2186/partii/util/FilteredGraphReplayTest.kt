package uk.ac.cam.jm2186.partii.util

import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.DefaultGraph
import org.graphstream.util.Filters
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.partii.GraphAssert
import uk.ac.cam.jm2186.partii.graph.GraphTestUtils

internal class FilteredGraphReplayTest {

    @Test
    fun replay_defaultBehavior() {
        val graph = GraphTestUtils.generateSmallGraph()
        graph.setAttribute("graph_attr", 1)
        graph.getNode<Node>(0).setAttribute("node_attr", 2)
        graph.getEdge<Edge>(0).setAttribute("edge_attr", 3)

        val replay = FilteredGraphReplay(GraphTestUtils.createGraphId())
        val replayed = DefaultGraph(GraphTestUtils.createGraphId())
        replay.addSink(replayed)
        replay.replay(graph)

        GraphAssert.assertGraphEquals("""
            DGS004
            null 0 0
            cg  "graph_attr":1
            an "0"
            cn "0"  "node_attr":2
            an "1"
            an "2"
            an "3"
            an "4"
            an "5"
            an "6"
            an "7"
            an "8"
            an "9"
            ae "0_1" "0"  "1"
            ce "0_1"  "edge_attr":3
            ae "2_6" "2"  "6"
            ae "5_8" "5"  "8"
            ae "5_7" "5"  "7"
            ae "4_5" "4"  "5"
            ae "1_3" "1"  "3"
            ae "3_4" "3"  "4"
            ae "3_5" "3"  "5"
            ae "2_4" "2"  "4"
            ae "0_5" "0"  "5"
            ae "0_8" "0"  "8"
            ae "2_7" "2"  "7"
            ae "3_7" "3"  "7"
            ae "4_7" "4"  "7"
            ae "0_9" "0"  "9"
            ae "3_9" "3"  "9"
            ae "5_9" "5"  "9"
            ae "6_9" "6"  "9"
            ae "8_9" "8"  "9"
        """, replayed)
    }

    @Test
    fun replay_filterAllElements() {
        val graph = GraphTestUtils.generateSmallGraph()
        graph.setAttribute("graph_attr", 1)
        graph.getNode<Node>(0).setAttribute("node_attr", 2)
        graph.getEdge<Edge>(0).setAttribute("edge_attr", 3)

        val replay = FilteredGraphReplay(
            GraphTestUtils.createGraphId(),
            nodeFilter = Filters.falseFilter(),
            edgeFilter = Filters.falseFilter()
        )
        val replayed = DefaultGraph(GraphTestUtils.createGraphId())
        replay.addSink(replayed)
        replay.replay(graph)
        println(replayed.stringRepresentation())

        GraphAssert.assertGraphEquals("""
            DGS004
            null 0 0
            cg  "graph_attr":1
        """, replayed)
    }
}
