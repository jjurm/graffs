package uk.ac.cam.jm2186.graffs.graph

import org.graphstream.stream.file.FileSourceEdge
import org.junit.jupiter.api.Test
import uk.ac.cam.jm2186.graffs.graph.GraphAssert.assertGraphEquals

internal class ReadGraphTest {

    @Test
    fun testFileSourceReadGraph() {
        val str = """
            0 1
            0 2
            1 2
        """
        val graph = FileSourceEdge(false).readGraph(str.byteInputStream(), id = "0")
        assertGraphEquals(
            """0
1 [1->0]
2 [2->1,2->2]""", graph
        )
    }

}
