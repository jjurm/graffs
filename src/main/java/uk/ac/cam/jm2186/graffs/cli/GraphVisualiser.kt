package uk.ac.cam.jm2186.graffs.cli

import org.graphstream.graph.Graph
import org.graphstream.ui.layout.Layouts
import org.graphstream.ui.view.Viewer

class GraphVisualiser {

    fun visualise(graph: Graph) {
        val viewer = Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
        viewer.addView(Viewer.DEFAULT_VIEW_ID, Viewer.newGraphRenderer())
        viewer.enableAutoLayout(Layouts.newLayoutAlgorithm())
    }

}
