package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.DefaultGraph
import org.graphstream.util.Filters
import org.junit.jupiter.api.Test

internal class FilteredGraphReplayTest {

    @Test
    fun replay_defaultBehavior() {
        val graph = GraphTestUtils.generateSmallGraph()
        graph.setAttribute("graph_attr", 1)
        graph.getNode<Node>(0).setAttribute("node_attr", 2)
        graph.getEdge<Edge>(0).setAttribute("edge_attr", 3)

        val replay =
            FilteredGraphReplay(GraphTestUtils.createGraphId())
        val replayed = DefaultGraph(GraphTestUtils.createGraphId())
        replay.addSink(replayed)
        replay.replay(graph)

        GraphAssert.assertGraphEquals(
            """
            {graph_attr=1}
            0{node_attr=2}
            1 [1->0_1{edge_attr=3}]
            2
            3 [3->1_3]
            4 [4->3_4,4->2_4]
            5 [5->4_5,5->3_5,5->0_5]
            6 [6->2_6]
            7 [7->5_7,7->2_7,7->3_7,7->4_7]
            8 [8->5_8,8->0_8]
            9 [9->0_9,9->3_9,9->5_9,9->6_9,9->8_9]""",
            replayed
        )
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

        GraphAssert.assertGraphEquals(
            """
            {graph_attr=1}""",
            replayed
        )
    }
}
