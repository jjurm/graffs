package uk.ac.cam.jm2186.graffs.robustness

import uk.ac.cam.jm2186.graffs.metric.MetricInfo
import uk.ac.cam.jm2186.graffs.storage.model.GraphCollection
import java.io.Serializable
import java.util.function.Supplier

typealias RobustnessMeasureId = String

interface RobustnessMeasure : Serializable {
    fun evaluate(metric: MetricInfo, graphCollection: GraphCollection): Double

    companion object {
        val map = mapOf<RobustnessMeasureId, RobustnessMeasureFactory>(
            "RankInstability" to RankInstabilityMeasure.Factory()
        )
    }
}

interface RobustnessMeasureFactory : Serializable, Supplier<RobustnessMeasure>
