package uk.ac.cam.jm2186.partii.graph

import org.graphstream.graph.Element
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.util.Filter
import uk.ac.cam.jm2186.partii.util.FilteredGraphReplay
import kotlin.random.Random

class RemovingEdgesGraphProducer(
    private val sourceGraph: SingleGraph,
    deletionRate: Double = 0.05,
    seed: Long
) : GraphProducer {

    private val id = sourceGraph.id + "-" + javaClass.name
    private var count = 0
    private val filteredReplay = FilteredGraphReplay(id, edgeFilter = RandomElementRemoverFilter(deletionRate, seed))

    override fun produce(): Graph {
        val graph = SingleGraph("$id-${count++}")
        filteredReplay.addSink(graph)
        return graph
    }

    override fun compute() {
        filteredReplay.replay(sourceGraph)
    }

    class RandomElementRemoverFilter<E : Element>(private val deletionRate: Double, seed: Long) : Filter<E> {

        private val random = Random(seed)
        private val removed = HashSet<E>()

        override fun isAvailable(e: E): Boolean {
            if (removed.contains(e)) return false
            if (random.nextDouble() < deletionRate) {
                removed.add(e)
                return false
            } else return true
        }

    }

}
