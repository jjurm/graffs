package uk.ac.cam.jm2186.graffs.graph.storage

import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.renjin.script.RenjinScriptEngineFactory
import org.renjin.sexp.*
import uk.ac.cam.jm2186.graffs.graph.ATTRIBUTE_NAME_EDGE_WEIGHT
import java.io.File
import javax.script.ScriptEngine

internal class RDataGraphLoader(fileFilter: File.() -> Boolean = { true }) : GraphLoader(fileFilter) {

    val engine: ScriptEngine = RenjinScriptEngineFactory().scriptEngine

    override fun load(file: File, id: String): Graph {
        engine.eval("""load("${file.invariantSeparatorsPath}", e <- new.env())""")
        val names = engine.eval("ls(e)") as StringVector
        val name = names.first { it.endsWith(".df") }

        val dataframe = engine.eval("e\$$name") as ListVector
        val protein1 = dataframe[0] as StringVector
        val protein2 = dataframe[1] as StringVector
        val combined_score = dataframe[2] as IntVector

        val graph = SingleGraph(id, false, true)
        (protein1 zip protein2 zip combined_score).forEach { (nodes, score) ->
            val (node1, node2) = nodes
            val edge = graph.addEdge<Edge>("$node1-$node2", node1, node2)
            edge.addAttribute(ATTRIBUTE_NAME_EDGE_WEIGHT, score)
        }
        return graph
    }

}
