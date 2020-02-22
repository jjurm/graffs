package uk.ac.cam.jm2186.graffs.metric

import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import uk.ac.cam.jm2186.graffs.graph.ATTRIBUTE_NAME_NODE_VALUE
import java.io.Serializable

typealias MetricId = String
typealias MetricResult = Pair<Double?, ByteArray?>

interface Metric : Serializable {
    fun evaluate(graph: Graph): MetricResult

    companion object {
        val map = mapOf<MetricId, MetricFactory>(
            "AverageDegree" to AverageDegreeMetric.Factory(),
            "BetweennessCentrality" to BetweennessCentralityMetric.Factory(),
            "PageRank" to PageRankMetric.Factory(),
            "DangalchevClosenessCentrality" to ClosenessCentralityMetric.DangalchevFactory()
        )

        internal fun removeNodeAttributesExceptV(graph: Graph) {
            graph.getEachNode<Node>().forEach { node ->
                node.attributeKeyIterator.retainIf { it == ATTRIBUTE_NAME_NODE_VALUE }
            }
        }

        private fun <T> MutableIterator<T>.retainIf(predicate: (T) -> Boolean) {
            while (hasNext()) {
                if (!predicate(next())) remove()
            }
        }
    }

}

interface MetricFactory : Serializable {
    fun createMetric(params: List<Number>): Metric
}
