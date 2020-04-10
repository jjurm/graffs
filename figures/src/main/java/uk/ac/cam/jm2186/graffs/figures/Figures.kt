package uk.ac.cam.jm2186.graffs.figures

import uk.ac.cam.jm2186.graffs.cli.GraphVisualiser
import uk.ac.cam.jm2186.graffs.graph.filterAtThreshold
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset


abstract class Figures : FigureContext {

    @Figure(
        "ecoli_giant_at_900",
        height = "10cm",
        caption = """An interaction network of proteins from the \textit{Escherichia coli} organism from the STRING database, thresholded at the $0.9$ score (high confidence). Only nodes of the giant component are shown."""
    )
    fun figure1() {
        val graph = GraphDataset("ecoli").loadGraph()
        val ecoli900 = graph.filterAtThreshold(900.0)
        log("the graph has ${ecoli900.nodeCount} nodes and ${ecoli900.edgeCount} edges")
        GraphVisualiser(ecoli900, true).screenshot(targetFile, false)
    }

}
