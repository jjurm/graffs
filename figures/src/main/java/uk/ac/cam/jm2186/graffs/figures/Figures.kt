package uk.ac.cam.jm2186.graffs.figures

import org.graphstream.algorithm.ConnectedComponents
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.ui.layout.springbox.implementations.SpringBox
import uk.ac.cam.jm2186.graffs.cli.GraphVisualiser
import uk.ac.cam.jm2186.graffs.graph.alg.giantComponent
import uk.ac.cam.jm2186.graffs.graph.computeLayout
import uk.ac.cam.jm2186.graffs.graph.copy
import uk.ac.cam.jm2186.graffs.graph.gen.filterAtThreshold
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.graph.style
import uk.ac.cam.jm2186.graffs.util.removeAll
import java.util.*


abstract class Figures : FigureContext {

    @Figure(
        "ecoli_giant_at_900",
        height = "10cm",
        caption = """An interaction network of proteins from the \textit{Escherichia coli} organism from the STRING database, thresholded at the $0.9$ score (high confidence). Only nodes of the giant component are shown."""
    )
    fun figure1() {
        val graph = GraphDataset("ecoli").loadGraph()
        val ecoli900 = graph.filterAtThreshold(900.0).giantComponent()
        log("the graph has ${ecoli900.nodeCount} nodes and ${ecoli900.edgeCount} edges")
        GraphVisualiser(ecoli900).screenshot(newTargetFile(), false)
    }

    @Figure(
        "simple_graph",
        height = "6cm",
        caption = """A small example of a \textsl{connected} \textsl{simple} graph (i.e. \textsl{undirected} and \textsl{anti-reflexive})."""
    )
    fun figureSimpleGraph() {
        var graph = GraphDataset("test").loadGraph().copy()
        graph.removeNode<Node>(0)
        graph = graph.giantComponent()

        log("${graph.nodeCount} nodes and ${graph.edgeCount} edges")
        styleBig1(graph)
        GraphVisualiser(graph).screenshot(newTargetFile(), false)
    }

    @Figure(
        "disconnecting_graph",
        height = "8cm",
        caption = """Illustrated is how thresholding a protein network results in one giant component (red) and multiple small disconnected components.
The left is a certain subgraph of the \textit{ecoli} dataset (all edges with confidence $>0.4$), the right graph has only edges with confidence $>0.5$."""
    )
    fun figureDisconnectedGraph() {
        var graph = GraphDataset("ecoli").loadGraph().copy()

        val baseNodes = listOf(0).map { graph.getNode<Node>(it) }
        // Find all of distance 2 from base nodes, except base nodes
        var targetNodes = baseNodes.toSet()
        repeat(2) {
            targetNodes = targetNodes + targetNodes.flatMap {
                it.getNeighborNodeIterator<Node>().asSequence().toList()
            }.toSet()
        }
        targetNodes = targetNodes - baseNodes

        // Only keep selected nodes, and only the giant component at threshold 0.4
        graph.iterator().removeAll { it !in targetNodes }
        graph = graph.filterAtThreshold(400.0).giantComponent()

        // Calculate layout
        val layout = SpringBox(false, Random(42))
        graph.computeLayout(layout, 0.95)

        // figure1
        graph.getNodeSet<Node>().forEach { it.style("size: 9px;") }
        graph.getEdgeSet<Edge>().forEach {
            it.style("size: 4px; fill-color: #0000A030;")
        }
        GraphVisualiser(graph, autoLayout = false).screenshot(newTargetFile(), false)

        // thresholding
        val graph2 = graph.filterAtThreshold(500.0)
        // mark red nodes and edges
        val components = ConnectedComponents()
        components.init(graph2)
        components.compute()
        graph2.getEachNode<Node>().forEach { node ->
            if (node in components.giantComponent) {
                node.style("fill-color: #E40000;")
            } else {
                node.style("size: 11px;")
            }
        }
        graph2.getEachEdge<Edge>().forEach { edge ->
            if (edge.getNode0() in components.giantComponent) {
                edge.style("fill-color: #A0000030;")
            } else {
                edge.style("size: 5px; fill-color: #0000A060;")
            }
        }
        GraphVisualiser(graph2, autoLayout = false).screenshot(newTargetFile(), false)
    }


    private fun styleBig1(graph: Graph) {
        graph.getNodeSet<Node>().forEach { it.style("size: 20px;") }
        graph.getEdgeSet<Edge>().forEach {
            it.style("size: 7px; fill-color: #0000A040;")
        }
    }

}
