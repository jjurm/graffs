package uk.ac.cam.jm2186.graffs.robustness

import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.storage.model.GraphCollection
import java.io.Serializable
import java.util.function.Supplier

typealias RobustnessMeasureId = String

interface RobustnessMeasure : Serializable {
    suspend fun evaluate(metric: MetricInfo, graphCollection: GraphCollection, metadata: GraphCollectionMetadata): Double

    companion object {
        val map = mapOf<RobustnessMeasureId, RobustnessMeasureFactory>(
            "RankIdentifiability" to RankIdentifiabilityMeasure.factory,
            "RankInstability" to RankInstabilityMeasure.factory
        )
    }
}

typealias RobustnessMeasureFactory = () -> RobustnessMeasure
