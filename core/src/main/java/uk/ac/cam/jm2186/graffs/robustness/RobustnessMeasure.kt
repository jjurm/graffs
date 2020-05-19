package uk.ac.cam.jm2186.graffs.robustness

import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.db.model.GraphCollection

typealias RobustnessMeasureId = String

interface RobustnessMeasure {
    suspend fun evaluate(metric: MetricInfo, graphCollection: GraphCollection, metadata: GraphCollectionMetadata): Double

    companion object {
        val map = mapOf<RobustnessMeasureId, RobustnessMeasureFactory>(
            "RankIdentifiability" to RankIdentifiabilityMeasure.factory,
            "RankInstability" to RankInstabilityMeasure.factory,
            "RankContinuity" to RankContinuityMeasure.factory
        )
    }
}

typealias RobustnessMeasureFactory = () -> RobustnessMeasure
