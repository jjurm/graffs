package uk.ac.cam.jm2186.graffs.figures

import uk.ac.cam.jm2186.graffs.cli.GraphVisualiser
import uk.ac.cam.jm2186.graffs.figures.FigureType.*
import uk.ac.cam.jm2186.graffs.graph.filterAtThreshold
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset

abstract class Figures : FigureContext {

    @Figure(
        "ecoli_giant_at_900", PNG,
        """An interaction network of proteins from the \textit{Escherichia coli} organism from the STRING database, thresholded at the $0.9$ score (high confidence). Only nodes of the giant component are shown."""
    )
    fun figure1() {
        val graph = GraphDataset("ecoli").loadGraph()
        val ecoli900 = graph.filterAtThreshold(900.0)
        GraphVisualiser(ecoli900, true).screenshot(targetFile, false)
    }

}
