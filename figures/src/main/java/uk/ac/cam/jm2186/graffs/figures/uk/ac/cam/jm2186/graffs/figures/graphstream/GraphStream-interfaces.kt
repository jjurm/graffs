package uk.ac.cam.jm2186.graffs.figures.uk.ac.cam.jm2186.graffs.figures.graphstream

interface Element {
    val id: String
    val index: Int
    val attributes: Map<String, Any>
}

interface Node: Element, Iterable<Node> {
    val edgeSet: Collection<Edge>
    val graph: Graph
}

interface Edge: Element {
    val node0: Node
    val node1: Node
}

interface Structure {
    val nodeSet: Collection<Node>
    val edgeSet: Collection<Edge>
}

interface Graph : Element, Structure, Iterable<Node>
