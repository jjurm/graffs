package uk.ac.cam.jm2186.graffs.graph.storage

import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.ObjectPool
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.impl.GenericObjectPool
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.renjin.script.RenjinScriptEngine
import org.renjin.script.RenjinScriptEngineFactory
import org.renjin.sexp.ListVector
import org.renjin.sexp.StringVector
import uk.ac.cam.jm2186.graffs.graph.ATTRIBUTE_NAME_EDGE_WEIGHT
import java.io.File

internal class RDataGraphLoader(fileFilter: File.() -> Boolean = { true }) : GraphLoader(fileFilter) {

    companion object {
        private val pool: ObjectPool<RenjinScriptEngine> = GenericObjectPool(RenjinFactory())
    }

    private class RenjinFactory : BasePooledObjectFactory<RenjinScriptEngine>() {
        private val renjinScriptEngineFactory = RenjinScriptEngineFactory()
        override fun wrap(obj: RenjinScriptEngine?): PooledObject<RenjinScriptEngine> = DefaultPooledObject(obj)
        override fun create(): RenjinScriptEngine = renjinScriptEngineFactory.scriptEngine
    }

    override fun load(file: File, id: String): Graph {
        val engine: RenjinScriptEngine = pool.borrowObject()

        engine.eval("""load("${file.invariantSeparatorsPath}", e <- new.env())""")
        val names = engine.eval("ls(e)") as StringVector
        val name = names.first { it.endsWith(".df") }
        val dataframe = engine.eval("e\$$name") as ListVector

        pool.returnObject(engine)

        val protein1 = dataframe[0] as StringVector
        val protein2 = dataframe[1] as StringVector

        @Suppress("UNCHECKED_CAST")
        val combined_score = dataframe[2] as Iterable<Number> // Either IntVector or DoubleVector

        val graph = SingleGraph(id, false, true)
        (protein1 zip protein2 zip combined_score).forEach { (nodes, score) ->
            val (node1, node2) = nodes
            val edge = graph.addEdge<Edge>("$node1-$node2", node1, node2)
            edge.addAttribute(ATTRIBUTE_NAME_EDGE_WEIGHT, score.toDouble())
        }
        return graph
    }

}
