package uk.ac.cam.jm2186.graffs.robustness

import org.graphstream.graph.Graph
import java.io.Serializable
import java.util.function.Supplier

typealias RobustnessMeasureId = String

interface RobustnessMeasure : Serializable {
    fun evaluate(originalGraph: Graph, distortedGraphs: List<Graph>): Double

    companion object {
        val map = mapOf<RobustnessMeasureId, RobustnessMeasureFactory>(
            "RiskInstability" to RiskInstabilityMeasure.Factory()
        )
    }
}

interface RobustnessMeasureFactory : Serializable, Supplier<RobustnessMeasure>
