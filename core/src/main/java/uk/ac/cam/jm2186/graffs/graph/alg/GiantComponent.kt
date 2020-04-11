package uk.ac.cam.jm2186.graffs.graph.alg

import org.graphstream.algorithm.ConnectedComponents
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.util.Filters
import uk.ac.cam.jm2186.graffs.graph.FilteredGraphReplay

fun Graph.giantComponent(): Graph {
    val components = ConnectedComponents()
    components.init(this)
    components.compute()
    val nodeFilter = Filters.isContained(components.giantComponent)

    val replay = FilteredGraphReplay(
        "$id-giant-replay",
        nodeFilter = nodeFilter,
        edgeFilter = Filters.byExtremitiesFilter(nodeFilter)
    )
    val giant = SingleGraph("$id-giant")
    replay.addSink(giant)
    replay.replay(this)
    return giant
}
