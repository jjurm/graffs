package uk.ac.cam.jm2186.partii

import org.apache.commons.lang3.StringUtils
import org.graphstream.graph.Graph
import org.junit.jupiter.api.Assertions
import uk.ac.cam.jm2186.partii.util.stringRepresentation

object GraphAssert {

    fun assertGraphEquals(expected: String, actual: Graph) =
        Assertions.assertEquals(
            expected.trimIndent(),
            actual.stringRepresentation()
        )

}
