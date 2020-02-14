package uk.ac.cam.jm2186.graffs.robustness

import org.apache.log4j.Logger
import org.graphstream.graph.Graph
import org.graphstream.graph.Node

class RiskInstabilityMeasure : RobustnessMeasure {

    class Factory : RobustnessMeasureFactory {
        override fun get() = RiskInstabilityMeasure()
    }

    val logger = Logger.getLogger(RiskInstabilityMeasure::class.java)

    override fun evaluate(originalGraph: Graph, distortedGraphs: List<Graph>): Double {
        val n = originalGraph.nodeCount

        // filter out up to 1% of highest ranked nodes
        val originalNodes =
            originalGraph.getNodeSet<Node>()
                .sortedByDescending { node -> node.getAttribute<Double>("v") }
        val topNodes = originalNodes.take(originalNodes.size / 100)

        if (topNodes.size < 5) {
            logger.warn("|Top 1% of nodes|=${topNodes.size} should not be less than 5")
        }

        val rankInstability = topNodes
            .map { node ->
                // calculate min and max
                distortedGraphs.fold(MinMaxAcc()) { acc, graph ->
                    val distortedNode = graph.getNode<Node?>(node.id)
                    val nodeValue = distortedNode?.getAttribute<Double>("v")
                    acc.add(nodeValue)
                }
            }
            .map(MinMaxAcc::range)
            .sumByDouble { v -> v ?: .0 } / n / topNodes.size

        return rankInstability
    }

    private class MinMaxAcc {
        var min = Acc<Double>(Math::min)
            private set
        var max = Acc<Double>(Math::max)
            private set

        fun add(value: Double?): MinMaxAcc {
            min.add(value)
            max.add(value)
            return this
        }

        fun range(): Double? {
            val (bottom, top) = min.value to max.value
            return if (bottom != null && top != null) (top - bottom) else null
        }

        private class Acc<T>(op: (T, T) -> T) {
            private val f = onNulls(op)
            var value: T? = null
            fun add(newValue: T?) {
                value = f(value, newValue)
            }
        }

        private companion object {
            fun <T> onNulls(op: (T, T) -> T): (T?, T?) -> T? = { v1, v2 ->
                when {
                    v1 == null -> v2
                    v2 == null -> null
                    else -> op(v1, v2)
                }
            }
        }
    }

}
