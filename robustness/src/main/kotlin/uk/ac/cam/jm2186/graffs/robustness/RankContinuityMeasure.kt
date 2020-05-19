package uk.ac.cam.jm2186.graffs.robustness

import uk.ac.cam.jm2186.graffs.db.model.GraphCollection
import uk.ac.cam.jm2186.graffs.graph.gen.AbstractEdgeThresholdGraphProducer
import uk.ac.cam.jm2186.graffs.metric.MetricInfo

class RankContinuityMeasure : RobustnessMeasure {

    companion object {
        val factory: RobustnessMeasureFactory = ::RankContinuityMeasure

        /**
         * A set of values for the proportion of nodes k considered to be the top ranking.
         * The default set is taken from the paper by Bozhilova et al.
         */
        val DEFAULT_K_VALUES: Sequence<Double> = 0.001..0.05 step 0.001

        /**
         *  An overall continuity measure is calculated based on how often the observed similarity is greater or equal
         *  to this threshold.
         *  The default value is taken from the paper by Bozhilova et al.
         */
        const val DEFAULT_K_SIMILARITY_THRESHOLD = 0.9
    }

    fun consecutiveRankingPairs(overallRanking: OverallNodeRanking): List<Pair<GraphAttributeNodeRanking, GraphAttributeNodeRanking>> {
        fun GraphAttributeNodeRanking.threshold(): Double {
            val number = graph.getNumber(AbstractEdgeThresholdGraphProducer.ATTRIBUTE_EDGE_THRESHOLD)
            if (number.isNaN()) {
                throw IllegalStateException("${this::class.simpleName} `${graph.id}` has no threshold attribute")
            }
            return number
        }

        return overallRanking
            .rankings
            .sortedBy { it.threshold() }
            .zipWithNext()
    }

    suspend fun consecutiveRankSimilarity(
        metadata: GraphCollectionMetadata,
        kValues: Sequence<Double>
    ): Sequence<Double> {
        val overallRanking = metadata.getOverallRanking()
        return consecutiveRankingPairs(overallRanking).asSequence()
            .map { (ranking1, ranking2) ->
                kValues.map { k ->
                    kSimilarity(k, overallRanking, ranking1, ranking2)
                }.average()
            }
    }

    override suspend fun evaluate(
        metric: MetricInfo,
        graphCollection: GraphCollection,
        metadata: GraphCollectionMetadata
    ): Double {
        val kValues = DEFAULT_K_VALUES
        val kThreshold = DEFAULT_K_SIMILARITY_THRESHOLD

        val rankContinuity = consecutiveRankSimilarity(metadata, kValues)
            .map { kSimilarity ->
                when (kSimilarity >= kThreshold) {
                    true -> 1
                    false -> 0
                }
            }
            .average()

        return rankContinuity
    }
}

internal infix fun ClosedFloatingPointRange<Double>.step(step: Double): Sequence<Double> {
    return object : Sequence<Double> {
        override fun iterator() = object : Iterator<Double> {
            private var i = 0
            private val nextValue: Double get() = start + i * step

            override fun next(): Double {
                val v = nextValue
                i++
                return v
            }

            override fun hasNext() = nextValue <= endInclusive
        }
    }
}
