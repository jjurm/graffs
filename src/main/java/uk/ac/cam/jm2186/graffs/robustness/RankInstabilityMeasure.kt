package uk.ac.cam.jm2186.graffs.robustness

import org.apache.log4j.Logger
import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.storage.model.GraphCollection
import kotlin.math.roundToInt

class RankInstabilityMeasure : RobustnessMeasure {

    companion object {
        val factory: RobustnessMeasureFactory = ::RankInstabilityMeasure
    }

    private val logger = Logger.getLogger(RankInstabilityMeasure::class.java)

    override suspend fun evaluate(
        metric: MetricInfo,
        graphCollection: GraphCollection,
        metadata: GraphCollectionMetadata
    ): Double {
        val overallRanking = metadata.getOverallRanking()
        val n = overallRanking.size

        // Filter out up to 1% of highest ranked nodes
        val topNodes = overallRanking.takeLast((n * 0.01).roundToInt())

        if (topNodes.size < 5) {
            logger.warn("|Top 1% of nodes|=${topNodes.size} is less than 5 (results may be based on too few samples)")
        }

        val rankInstability = topNodes
            .map { nodeId ->
                // Calculate min and max rank
                overallRanking.rankings.fold(RankRange()) { acc, ranking ->
                    acc.add(ranking.getRank(nodeId))
                }
            }
            .map(RankRange::range)
            .sumBy { v -> v ?: 0 }.toDouble() / n / topNodes.size

        return rankInstability
    }

    private class RankRange {
        var min = Acc<Int>(Math::min)
            private set
        var max = Acc<Int>(Math::max)
            private set

        fun add(value: Rank): RankRange {
            min.add(value.rankValue)
            max.add(value.rankValue)
            return this
        }

        fun range(): Int? {
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
