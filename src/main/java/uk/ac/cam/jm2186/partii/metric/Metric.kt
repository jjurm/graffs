package uk.ac.cam.jm2186.partii.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import java.io.Serializable

typealias MetricResult = Pair<Double?, ByteArray?>

interface Metric : Serializable {

    fun evaluate(graph: Graph): MetricResult

    companion object {
        fun removeNodeAttributesExceptV(graph: Graph) {
            graph.getEachNode<Node>().forEach { node->
                node.attributeKeyIterator.retainIf { it == "v" }
            }
        }

        private fun <T> MutableIterator<T>.retainIf(predicate: (T) -> Boolean) {
            while (hasNext()) {
                if (!predicate(next())) remove()
            }
        }
    }

}
