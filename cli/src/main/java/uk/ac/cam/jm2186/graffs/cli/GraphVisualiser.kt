package uk.ac.cam.jm2186.graffs.cli

import org.apache.commons.lang3.SystemUtils
import org.graphstream.graph.Graph
import org.graphstream.stream.file.FileSinkImages
import org.graphstream.ui.layout.Layout
import org.graphstream.ui.layout.springbox.implementations.SpringBox
import org.graphstream.ui.view.Viewer
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.IOException
import java.util.*

class GraphVisualiser(
    val graph: Graph,
    val seed: Long = 42,
    val autoLayout: Boolean = true
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
        if (autoLayout) {
            viewer.enableAutoLayout(SpringBox(false, Random(seed)))
        } else {
            viewer.disableAutoLayout()
        }
    }

    fun screenshot(
        file: File,
        display: Boolean = true,
        resolution: FileSinkImages.Resolution = FileSinkImages.CustomResolution(1200, 1200)
    ) {
        val output = CustomFileSinkImages(FileSinkImages.OutputType.png, resolution).apply {
            setStyleSheet(stylesheet)
            setQuality(FileSinkImages.Quality.HIGH)

            if (autoLayout) {
                setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE)
                setLayout(SpringBox(false, Random(seed)))
                setLayoutStabilizationLimit(0.95)
            } else {
                setLayoutPolicy(FileSinkImages.LayoutPolicy.NO_LAYOUT)
            }
        }
        output.writeAll(graph, file.path)

        if (display && !GraphicsEnvironment.isHeadless()) {
            when {
                SystemUtils.IS_OS_WINDOWS -> Runtime.getRuntime().exec(arrayOf("cmd", "/C", file.path))
                SystemUtils.IS_OS_UNIX -> Runtime.getRuntime().exec(arrayOf("xdg-open", file.path))
            }
        }
    }

    class CustomFileSinkImages(type: OutputType, resolution: Resolution) : FileSinkImages(type, resolution) {
        fun setLayout(layout: Layout) {
            this.layout?.let {
                gg.removeSink(it)
                it.removeAttributeSink(gg)
            }

            this.layout = layout
            gg.addSink(layout)
            layout.addAttributeSink(gg)
        }
    }
}
