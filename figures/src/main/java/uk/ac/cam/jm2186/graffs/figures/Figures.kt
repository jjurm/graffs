package uk.ac.cam.jm2186.graffs.figures

import org.graphstream.algorithm.ConnectedComponents
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.ui.layout.springbox.implementations.SpringBox
import uk.ac.cam.jm2186.graffs.cli.GraphVisualiser
import uk.ac.cam.jm2186.graffs.graph.*
import uk.ac.cam.jm2186.graffs.graph.alg.giantComponent
import uk.ac.cam.jm2186.graffs.graph.gen.filterAtThreshold
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.metric.*
import uk.ac.cam.jm2186.graffs.util.removeAll
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt


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

    val simpleGraph by lazy {
        val graph = GraphDataset("test").loadGraph().copy()
        graph.removeNode<Node>(0)
        graph.giantComponent()
    }

    @Figure(
        "simple_graph",
        height = "6cm",
        caption = """A small example of a \textsl{connected} \textsl{simple} graph (i.e. \textsl{undirected} and \textsl{anti-reflexive})."""
    )
    fun figureSimpleGraph() {
        val graph = simpleGraph.copy()
        log("${graph.nodeCount} nodes and ${graph.edgeCount} edges")
        styleBig1(graph)
        GraphVisualiser(graph).screenshot(newTargetFile(), false)
    }

    @Figure(
        "disconnecting_graph",
        height = "8cm",
        caption = """Illustrated is how thresholding (a subgraph of) a protein network (left) results in one giant component (red) and multiple small disconnected components.
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

    @Figure(
        "simplegraph_by_some_metrics",
        height = "5cm",
        caption = """A simple graph with each node's diameter proportional to its \textsl{degree} (1), \textsl{betweenness centrality} (2), and \textsl{local clustering} (3).
In this particular graph, (1) and (2) show similar characteristics (greater value for more ``central'' nodes), whereas local clustering is significantly different."""
    )
    fun figureByDegree() {
        val metrics = listOf<MetricInfo>(
            DegreeMetric,
            BetweennessCentralityMetric,
            LocalClusteringMetric
        )

        metrics.forEach { metric ->
            val graph = simpleGraph.copy()

            colorNodesByMetric(
                graph, metric, sizeMax = when (metric) {
                    LocalClusteringMetric -> 60.0
                    else -> 100.0
                }
            )
            graph.getEdgeSet<Edge>().forEach {
                it.style("size: 4px; fill-color: #0000A040;")
            }
            GraphVisualiser(graph, seed = 43).screenshot(newTargetFile(), false)
        }
    }

    @Figure(
        "graph_example_scored_edges",
        height = "5cm",
        caption = """A tiny subgraph of the \textsl{ecoli} dataset with scored edges"""
    )
    fun figureScoredEdges() {
        var graph = GraphDataset("ecoli").loadGraph()

        val mustHaveNeighbours = graph.getNode<Node>(0).getNeighborNodeIterator<Node>().asSequence()
            .toList().shuffled(Random(44)).take(2)
        log("mustHaveNeighbours: ${mustHaveNeighbours.size}")
        val keep = graph.filter { node ->
            node.degree < 320 && mustHaveNeighbours.all { node.hasEdgeBetween(it) }
        }
        log("to keep: ${keep.size}")
        log("degrees: ${keep.map { it.degree }}")
        graph = graph.subgraph(nodeSet = keep).giantComponent()

        graph.getNodeSet<Node>().forEach { it.style("size: 50px;") }
        graph.getEdgeSet<Edge>().forEach { edge ->
            val w = edge.getNumberAttribute(ATTRIBUTE_NAME_EDGE_WEIGHT) / 1000.0
            val size = (w * 45) + 4
            edge.style("size: ${size}px; fill-color: #0000A060;")
        }
        GraphVisualiser(graph).screenshot(newTargetFile(), false)
    }

    @Figure(
        "data_model_diagram",
        caption = """Data model diagram showing \textsl{persistence schema}, i.e. entities stored in the database.
The arrows indicate \textsl{association} links, i.e. ``has a'' or ``refers to'' relationships.
The diagram is created from the Java Persistence API schema inferred from the source code."""
    )
    fun diagramDataModel() {
        convertSvgToPdf(File("diagrams/data-model2.svg"), newTargetFile(FileType.PDF))
    }

    @Figure(
        "data_model_classes_diagram",
        caption = """Inheritance hierarchy of the (Kotlin) classes underlying the persistence model presented in \autoref{fig:data_model_diagram}.
The arrows indicate \textsl{inheritance} (``is a'') relationships between classes."""
    )
    fun diagramDataModelClasses() {
        convertSvgToPdf(File("diagrams/data-model-classes.svg"), newTargetFile(FileType.PDF))
    }

    @Figure(
        "graphstream_diagram",
        caption = """A simplified diagram of the most relevant interfaces from the GraphStream library"""
    )
    fun diagramGraphstream() {
        convertSvgToPdf(File("diagrams/graphstream.svg"), newTargetFile(FileType.PDF))
    }


    //----------

    private fun colorNodesByMetric(graph: Graph, metric: MetricInfo, sizeMin: Double = 6.0, sizeMax: Double = 60.0) {
        metric.evaluateSingle(graph)
        val values = graph.getNodeSet<Node>().map { it.getNumberAttribute(metric.attributeName) }
        val max = values.max()!!
        val min = values.min()!!

        graph.getNodeSet<Node>().forEach {
            val value = it.getNumberAttribute(metric.attributeName)
            val size = (value - min) / (max - min) * (sizeMax - sizeMin) + sizeMin
            it.style("size: ${size.roundToInt()}px;")
        }
    }


    private fun styleBig1(graph: Graph) {
        graph.getNodeSet<Node>().forEach { it.style("size: 20px;") }
        graph.getEdgeSet<Edge>().forEach {
            it.style("size: 7px; fill-color: #0000A040;")
        }
    }

    private fun convertSvgToPdf(`in`: File, out: File) {
        val process = Runtime.getRuntime().exec(
            arrayOf("inkscape", "-D", "-z", "--file", `in`.path, "--export-pdf", out.path, "--export-area-drawing")
        )
        if (process.waitFor() != 0) throw IOException("Could not run `inkscape`")
    }

}
