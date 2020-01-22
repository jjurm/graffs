package uk.ac.cam.jm2186.partii.cli

import com.github.ajalt.clikt.core.CliktCommand
import org.graphstream.graph.Graph
import org.graphstream.ui.layout.Layouts
import org.graphstream.ui.view.Viewer

abstract class AbstractVisualiseSubcommand : CliktCommand(
    name = "viz",
    help = "Visualise graph"
) {

    abstract fun getGraph(): Graph

    override fun run() {
        val viewer = Viewer(getGraph(), Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
        viewer.addView(Viewer.DEFAULT_VIEW_ID, Viewer.newGraphRenderer())
        viewer.enableAutoLayout(Layouts.newLayoutAlgorithm())
    }
}
