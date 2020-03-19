package uk.ac.cam.jm2186.graffs.graph

import kotlin.random.Random

class RandomEdgeThresholdGraphProducer(
    seed: Long,
    lowThreshold: Double,
    highThreshold: Double
) : AbstractEdgeThresholdGraphProducer(
    getThresholds = { n ->
        val random = Random(seed)
        List(n) { random.nextDouble(lowThreshold, highThreshold) }
    }
) {

    companion object : GraphProducerInfo {
        override val id: GraphProducerId = "threshold-random"
        override val factory: GraphProducerFactory = {seed, params ->
            RandomEdgeThresholdGraphProducer(
                seed,
                params[0].toDouble(),
                params[1].toDouble()
            )
        }
    }

    override val id = RandomEdgeThresholdGraphProducer.id

}
