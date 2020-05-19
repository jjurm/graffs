package uk.ac.cam.jm2186.graffs.graph.gen

import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

class RandomEdgeThresholdGraphProducer(
    seed: Long,
    lowThreshold: Double,
    highThreshold: Double,
    coroutineScope: CoroutineScope
) : AbstractEdgeThresholdGraphProducer(
    coroutineScope = coroutineScope,
    getThresholds = { n ->
        val random = Random(seed)
        List(n) { random.nextDouble(lowThreshold, highThreshold) }
    }
) {

    companion object : GraphProducerInfo {
        override val id: GraphProducerId = "threshold-random"
        override val factory: GraphProducerFactory = { seed, params, coroutineScope ->
            RandomEdgeThresholdGraphProducer(
                seed,
                params[0].toDouble(),
                params[1].toDouble(),
                coroutineScope
            )
        }
    }

    override val id = Companion.id

}
