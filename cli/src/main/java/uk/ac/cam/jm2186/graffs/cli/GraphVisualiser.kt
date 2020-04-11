package uk.ac.cam.jm2186.graffs.cli

import org.apache.commons.lang3.SystemUtils
import org.graphstream.graph.Graph
import org.graphstream.stream.file.FileSinkImages
import org.graphstream.ui.layout.Layouts
import org.graphstream.ui.view.Viewer
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.IOException

class GraphVisualiser(
    val graph: Graph
) {
    companion object {
        init {
            System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")
        }
    }

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
        val output = FileSinkImages(FileSinkImages.OutputType.png, FileSinkImages.Resolutions.UXGA).apply {
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

}
