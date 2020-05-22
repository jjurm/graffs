package uk.ac.cam.jm2186.graffs.robustness

import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.db.model.GraphCollection

object RobustnessMeasures {
    val map = mapOf<RobustnessMeasureId, RobustnessMeasureFactory>(
        "RankIdentifiability" to RankIdentifiabilityMeasure.factory,
        "RankInstability" to RankInstabilityMeasure.factory,
        "RankContinuity" to RankContinuityMeasure.factory
    )
}

interface RobustnessMeasure {
    suspend fun evaluate(
        metric: MetricInfo,
        graphCollection: GraphCollection,
        metadata: GraphCollectionMetadata
    ): Double
}

typealias RobustnessMeasureFactory = () -> RobustnessMeasure

fun Map<RobustnessMeasureId, Double>.overallRobustness() = map { (id, value) ->
    when (id) {
        "RankInstability" -> -value
        else -> value
    }
}.sum()
