package uk.ac.cam.jm2186.graffs.figures

import com.github.ajalt.clikt.core.CliktCommand
import org.graphstream.algorithm.ConnectedComponents
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.stream.file.FileSinkImages
import org.graphstream.ui.layout.springbox.implementations.SpringBox
import uk.ac.cam.jm2186.graffs.cli.Graffs
import uk.ac.cam.jm2186.graffs.cli.GraphVisualiser
import uk.ac.cam.jm2186.graffs.graph.*
import uk.ac.cam.jm2186.graffs.graph.alg.giantComponent
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.metric.*
import uk.ac.cam.jm2186.graffs.util.removeAll
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


abstract class Figures : FigureContext {

    @Figure(
        "ecoli_giant_at_900",
        beginEndFigure = false,
        gfxArgs = """width=\linewidth""",
        caption = """An interaction network of proteins from the \textit{Escherichia coli} organism from the STRING database, thresholded at the $0.9$ score (high confidence). Only nodes of the giant component are shown."""
    )
    fun figure1() {
        val ecoli900 = GraphDataset("ecoli").loadGraph().filterAtThreshold(900.0).giantComponent()
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
        wrapfigureArgs = """[11]{R}{0.31\textwidth}""",
        vspaceAround = "-10mm",
        gfxArgs = """width=\linewidth""",
        caption = """A small example of a \textsl{connected} \textsl{simple} graph (\textsl{undirected} and \textsl{anti-reflexive})."""
    )
    fun figureSimpleGraph() {
        val graph = simpleGraph.copy()
        log("${graph.nodeCount} nodes and ${graph.edgeCount} edges")
        styleBig1(graph)
        GraphVisualiser(graph).screenshot(newTargetFile(), false)
    }

    @Figure(
        "disconnecting_graph",
        gfxArgs = "height=6cm",
        vspaceAround = "-4mm",
        caption = """Illustrated is how thresholding (a subgraph of) a protein network (left) results in one giant component (red) and multiple small disconnected components.
The left is a certain subgraph of the \textit{ecoli} dataset (all edges with confidence $>0.4$), the right graph has only edges with confidence $>0.5$."""
    )
    fun figureDisconnectedGraph() {
        val graph = smallEcoliSubset(400.0)

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
        gfxArgs = "height=5cm",
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
        wrapfigureArgs = """[11]{R}{0.24\textwidth}""",
        gfxArgs = """width=\linewidth,trim=0 3.4cm 0 3.2cm,clip,angle=90,origin=c""",
        caption = """A tiny subgraph of the \texttt{ecoli} dataset with the width of edges proportional to their scores"""
    )
    fun figureScoredEdges() {
        var graph = GraphDataset("ecoli").loadGraph().copy()

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
            val w = edge.getNumberAttribute(EDGE_ATTRIBUTE_WEIGHT) / 1000.0
            val size = (w * 45) + 4
            edge.style("size: ${size}px; fill-color: #0000A060;")
        }
        GraphVisualiser(graph).screenshot(newTargetFile(), false)
    }

    @Figure(
        "data_model_diagram",
        gfxArgs = """width=0.9\linewidth""",
        caption = """Data model diagram showing \textsl{persistence schema}, i.e. entities stored in the database.
The arrows indicate \textsl{association} links, i.e. ``has a'' or ``refers to'' relationships.
The diagram is created from the Java Persistence API schema inferred from the source code."""
    )
    fun diagramDataModel() {
        convertSvgToPdf(File("diagrams/data-model2.svg"), newTargetFile(FileType.PDF))
    }

    @Figure(
        "data_model_classes_diagram",
        gfxArgs = """width=\linewidth""",
        caption = """\textsl{Entity classes} (i.e. classes defining the persistence model presented in \autoref{fig:data_model_diagram}), and their inheritance hierarchy.
The arrows indicate \textsl{inheritance} (``is a'') relationships between classes."""
    )
    fun diagramDataModelClasses() {
        convertSvgToPdf(File("diagrams/data-model-classes.svg"), newTargetFile(FileType.PDF))
    }

    @Figure(
        "graphstream_diagram",
        figurePos = "h",
        gfxArgs = """width=0.8\linewidth""",
        caption = """A simplified diagram of the most relevant interfaces from the GraphStream library"""
    )
    fun diagramGraphstream() {
        convertSvgToPdf(File("diagrams/graphstream.svg"), newTargetFile(FileType.PDF))
    }

    @Figure(
        "data_dir_structure",
        caption = """An example structure of the \texttt{data} directory"""
    )
    fun treeDatasets() {
        val root = File("data")

        val sb = StringBuilder(
            """\begin{tikzpicture}%
\draw[color=black!60!white]"""
        )
        var nextId = 0
        fun File.escapedName() = name.replace("_", "\\_")
        fun dir(parentId: String, d: File) {
            val id = "i" + (nextId++)
            sb.append("\n\\FTdir($parentId,$id,${d.escapedName()}){")
            d.listFiles()!!.forEach { child ->
                if (child.isDirectory) {
                    dir(id, child)
                } else {
                    sb.append("\n\\FTfile($id,${child.escapedName()})")
                }
            }
            sb.append("\n}")
        }
        dir("\\FTroot", root)
        sb.append(";\n\\end{tikzpicture}\n")

        newTargetFile(FileType.TEX).writeText(sb.toString(), Charsets.UTF_8)
    }

    @Figure(
        "cli_commands_hierarchy",
        caption = """Hierarchy of supported command-line interface commands, and their brief description""",
        captionPos = CaptionPos.TOP
    )
    fun treeCliCommands() {
        val sb = StringBuilder(
            """\begin{flushleft}
\setlength{\DTbaselineskip}{11pt}
\small
\dirtree{%
"""
        )

        class CliktTree(val cmd: CliktCommand) {
            val children: Iterable<CliktTree>
                get() {
                    val f = CliktCommand::class.memberProperties.first { it.name == "_subcommands" }
                    f.isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    val children = f.get(cmd) as List<CliktCommand>
                    return children.map { CliktTree(it) }
                }

            override fun toString(): String {
                val helpText = cmd.commandHelp.substringBefore("\n")
                    .takeIf { it.isNotEmpty() }
                    ?.let { """ \scriptsize \dotfill\ \begin{minipage}[t]{10.6cm}$it\end{minipage}""" }
                    ?: ""
                return cmd.commandName + helpText
            }
        }

        fun rec(tree: CliktTree, level: Int) {
            sb.append(".$level $tree.\n")
            tree.children.forEach { child -> rec(child, level + 1) }
        }
        rec(CliktTree(Graffs()), 1)
        sb.append("}\n\\end{flushleft}")

        newTargetFile(FileType.TEX).writeText(sb.toString(), Charsets.UTF_8)
    }

    @Figure("plot_edge_deletion_per_step", generateTex = false)
    fun edgeDeletionPerStep() {
        val sb = StringBuffer("dataset,threshold,deleted\n")

        val datasets = listOf("pvivax", "ecoli", "yeast")
        datasets.forEach { dataset ->
            val graph = GraphDataset(dataset).loadGraph()
            var g = graph.filterAtThreshold(600.0)
            (600..900 step 10).drop(1).forEach { threshold ->
                val n = g.edgeCount
                g = g.filterAtThreshold(threshold.toDouble())
                val n2 = g.edgeCount
                val r = (n - n2).toDouble() / n
                sb.append("$dataset,$threshold,$r\n")
            }
        }
        newTargetFile(FileType.CSV).writeText(sb.toString())
    }

    @Figure("histogram_edges", generateTex = false)
    fun histogramEdges() {
        val sb = StringBuffer("dataset,weights\n")

        val datasets = listOf("pvivax", "ecoli", "yeast")
        datasets.forEach { dataset ->
            val graph = GraphDataset(dataset).loadGraph()
            val weights = graph.getEachEdge<Edge>().map {
                val w = it.getNumberAttribute(EDGE_ATTRIBUTE_WEIGHT)
                w.toInt()
            }
            sb.append("$dataset,${weights.joinToString(";")}\n")
        }
        newTargetFile(FileType.CSV).writeText(sb.toString())
    }

    @Figure(
        "thresholding_array",
        gfxArgs = """width=0.2\linewidth""",
        caption = """Visualised effect of thresholding confidence scores.
The 5 graphs correspond to a small subset of the \textsl{ecoli} dataset thresholded at linearly spaced values between $0.55$ and $0.95$."""
    )
    fun figureThresholdingArray() {
        var base = smallEcoliSubset(550.0)

        // Calculate and keep the layout
        val layout = SpringBox(false, Random(43))
        base.computeLayout(layout, 0.95)

        // Set style
        base.getNodeSet<Node>().forEach { it.style("size: 9px;") }
        base.getEdgeSet<Edge>().forEach {
            it.style("size: 4px; fill-color: #0000A080;")
        }

        (550..950 step 100).forEachIndexed { i, threshold ->
            base = base.filterAtThreshold(threshold.toDouble(), i = i)
            GraphVisualiser(base, autoLayout = false).screenshot(newTargetFile(), false)
        }
    }

    @Figure("cover_graph_img", generateTex = false)
    fun coverGraphImg() {
        val graph = smallEcoliSubset(450.0)
        val layout = SpringBox(false, Random(7))
        graph.computeLayout(layout, 0.95)

        // figure1
        graph.getNodeSet<Node>().forEach { it.style("size: 20px;") }
        graph.getEdgeSet<Edge>().forEach {
            it.style("size: 6px; fill-color: #00000045;")
        }
        GraphVisualiser(graph, autoLayout = false).screenshot(
            newTargetFile(), false,
            resolution = FileSinkImages.CustomResolution(2400, 2400)
        )
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

    private fun smallEcoliSubset(threshold: Double): Graph {
        var base = GraphDataset("ecoli").loadGraph().copy()

        val baseNodes = listOf(0).map { base.getNode<Node>(it) }
        // Find all of distance 2 from base nodes, except base nodes
        var targetNodes = baseNodes.toSet()
        repeat(2) {
            targetNodes = targetNodes + targetNodes.flatMap {
                it.getNeighborNodeIterator<Node>().asSequence().toList()
            }.toSet()
        }
        targetNodes = targetNodes - baseNodes

        // Only keep selected nodes, and only the giant component at threshold 0.4
        base.iterator().removeAll { it !in targetNodes }
        base = base.filterAtThreshold(threshold).giantComponent()
        return base
    }

}
