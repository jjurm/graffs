package uk.ac.cam.jm2186.graffs.graph.storage

import org.graphstream.stream.file.FileSourceEdge
import uk.ac.cam.jm2186.graffs.graph.EDGE_ATTRIBUTE_WEIGHT
import java.io.StreamTokenizer

/**
 * Similar to [FileSourceEdge] but also handles input from https://string-db.org/ and http://konect.uni-koblenz.de/.
 */
class FileSourceEdgeOptionalWeight(edgesAreDirected: Boolean) : FileSourceEdge(edgesAreDirected) {

    override fun configureTokenizer(tok: StreamTokenizer) {
        tok.apply {
            resetSyntax()

            wordChars('a'.toInt(), 'z'.toInt())
            wordChars('A'.toInt(), 'Z'.toInt())
            wordChars(128 + 32, 255)
            whitespaceChars(0, ' '.toInt())
            commentChar('/'.toInt())
            commentChar('%'.toInt())
            quoteChar('"'.toInt())
            quoteChar('\''.toInt())
            //parseNumbers()

            wordChars('.'.toInt(), '.'.toInt())
            wordChars('0'.toInt(), '9'.toInt())

            if (COMMENT_CHAR > 0) commentChar(COMMENT_CHAR)
            quoteChar(QUOTE_CHAR)
            eolIsSignificant(eol_is_significant)
            wordChars('_'.toInt(), '_'.toInt())
        }
    }

    override fun nextEvents(): Boolean {
        val id1 = getWordOrNumberOrStringOrEolOrEof()

        if (id1 == "EOL") {
            // Empty line.
        } else if (id1 == "EOF") {
            return false
        } else if (id1 == "protein1") {
            // skip whole line
            var id2 = id1
            while (id2 != "EOL") {
                id2 = getWordOrNumberOrStringOrEolOrEof()
            }
        } else {
            declareNode(id1)

            var id2 = getWordOrNumberOrStringOrEolOrEof()

            if (id2 != "EOL") {
                if (id1 != id2) {
                    val edgeId = Integer.toString(edgeid++)
                    declareNode(id2)
                    sendEdgeAdded(graphName, edgeId, id1, id2, directed)

                    // try to read edge weight
                    id2 = getWordOrNumberOrStringOrEolOrEof()
                    if (id2 != "EOL") {
                        val weight = id2.toDouble()
                        sendEdgeAttributeAdded(
                            graphName, edgeId,
                            EDGE_ATTRIBUTE_WEIGHT, weight
                        )
                    }
                }

                // ignore all other tokens on the line
                while (id2 != "EOL") {
                    id2 = getWordOrNumberOrStringOrEolOrEof()
                }
            }
        }

        return true
    }
}
