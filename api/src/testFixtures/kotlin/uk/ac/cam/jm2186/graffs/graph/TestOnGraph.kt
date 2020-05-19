package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.graph.Graph
import org.junit.jupiter.api.BeforeEach

open class TestOnGraph {

    lateinit var graph: Graph

    @BeforeEach
    fun setup() {
        graph = GraphTestUtils.generateSmallGraph()
    }

}
