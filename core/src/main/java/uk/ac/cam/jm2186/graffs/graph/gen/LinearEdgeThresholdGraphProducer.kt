package uk.ac.cam.jm2186.graffs.graph.gen

class LinearEdgeThresholdGraphProducer(
    lowThreshold: Double,
    highThreshold: Double
) : AbstractEdgeThresholdGraphProducer(
    getThresholds = { n ->
        if (n == 1) listOf(lowThreshold)
        else List(n) { i ->
            lowThreshold + (highThreshold - lowThreshold) * (i.toDouble() / (n - 1))
        }
    }
) {

    companion object : GraphProducerInfo {
        override val id: GraphProducerId = "threshold-linear"
        override val factory: GraphProducerFactory = { _, params ->
            LinearEdgeThresholdGraphProducer(
                lowThreshold = params[0].toDouble(),
                highThreshold = params[1].toDouble()
            )
        }
    }

    override val id = Companion.id

}
