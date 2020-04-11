package uk.ac.cam.jm2186.graffs

import org.graphstream.graph.Graph
import org.junit.jupiter.api.Assertions
import uk.ac.cam.jm2186.graffs.graph.storage.stringRepresentation

object GraphAssert {

    fun assertGraphEquals(expected: String, actual: Graph) =
        Assertions.assertEquals(
            expected.trimIndent(),
            actual.stringRepresentation()
        )

}
