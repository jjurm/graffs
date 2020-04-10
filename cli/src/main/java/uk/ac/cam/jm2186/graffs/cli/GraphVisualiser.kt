package uk.ac.cam.jm2186.graffs.cli

import org.apache.commons.lang3.SystemUtils
import org.graphstream.algorithm.ConnectedComponents
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.Graphs
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file.FileSinkImages
import org.graphstream.ui.layout.Layouts
import org.graphstream.ui.view.Viewer
import org.graphstream.util.Filters
import uk.ac.cam.jm2186.graffs.util.FilteredGraphReplay
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.IOException

class GraphVisualiser(
    sourceGraph: Graph,
    giantComponent: Boolean = false
) {
    private val graph: Graph = if (giantComponent) giantComponent(sourceGraph) else Graphs.clone(sourceGraph)
    private val stylesheet: String

    init {
        val resource = Thread.currentThread().contextClassLoader.getResourceAsStream("graph-viz.css")
            ?: throw IOException("cannot load bundled graph-viz.css")
        stylesheet = resource.bufferedReader().readText()
    }

    fun display() {
        graph.addAttribute("ui.stylesheet", stylesheet)
        graph.addAttribute("ui.antialias")
        graph.addAttribute("ui.quality")

        val viewer = Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
        viewer.addView(Viewer.DEFAULT_VIEW_ID, Viewer.newGraphRenderer())
        viewer.enableAutoLayout(Layouts.newLayoutAlgorithm())
    }

    fun screenshot(file: File, display: Boolean = true) {
        val output = FileSinkImages(FileSinkImages.OutputType.png, FileSinkImages.Resolutions.HD1080).apply {
            setStyleSheet(stylesheet)
            setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE)
            setQuality(FileSinkImages.Quality.HIGH)
            setLayoutStabilizationLimit(0.95)
        }
        output.writeAll(graph, file.path)

        if (display && !GraphicsEnvironment.isHeadless()) {
            when {
                SystemUtils.IS_OS_WINDOWS -> Runtime.getRuntime().exec(arrayOf("cmd", "/C", file.path))
                SystemUtils.IS_OS_UNIX -> Runtime.getRuntime().exec(arrayOf("xdg-open", file.path))
            }
        }
    }

    private fun giantComponent(graph: Graph): Graph {
        val components = ConnectedComponents()
        components.init(graph)
        components.compute()
        val nodeFilter = Filters.isContained(components.giantComponent)

        val replay = FilteredGraphReplay(
            graph.id + "-giant-replay",
            nodeFilter = nodeFilter,
            edgeFilter = Filters.byExtremitiesFilter(nodeFilter)
        )
        val giant = SingleGraph(graph.id + "-giant")
        replay.addSink(giant)
        replay.replay(graph)
        return giant
    }

}
