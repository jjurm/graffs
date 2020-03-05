package uk.ac.cam.jm2186.graffs.robustness

import uk.ac.cam.jm2186.graffs.graph.AbstractEdgeThresholdGraphProducer
import uk.ac.cam.jm2186.graffs.graph.getNumberAttribute
import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.storage.model.GraphCollection

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

    override suspend fun evaluate(
        metric: MetricInfo,
        graphCollection: GraphCollection,
        metadata: GraphCollectionMetadata
    ): Double {

        val overallRanking = metadata.getOverallRanking()
        val rankingPairs = overallRanking.rankings
            .map { ranking ->
                val threshold = ranking.graph.getNumberAttribute(
                    AbstractEdgeThresholdGraphProducer.ATTRIBUTE_EDGE_THRESHOLD
                )
                threshold to ranking
            }
            .sortedBy(Pair<Double, GraphAttributeNodeRanking>::first)
            .map(Pair<Double, GraphAttributeNodeRanking>::second)
            .zipWithNext()
        val kValues = DEFAULT_K_VALUES
        val kThreshold = DEFAULT_K_SIMILARITY_THRESHOLD

        val rankContinuity = rankingPairs.asSequence()
            .flatMap { (ranking1, ranking2) ->
                kValues.map { k ->
                    kSimilarity(k, overallRanking, ranking1, ranking2)
                }
            }
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
