package uk.ac.cam.jm2186.graffs.storage.model

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.GraphProducerFactory
import uk.ac.cam.jm2186.graffs.storage.AbstractJpaPersistable
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType

@Entity
class GeneratedGraph(
    val sourceGraph: GraphDataset,
    val generator: Class<out GraphProducerFactory>,
    val seed: Long,
    @ElementCollection(fetch = FetchType.EAGER)
    val params: List<Number>
) : AbstractJpaPersistable<Long>() {

    fun produceGenerated() : Graph {
        val generatorFactory = generator.getDeclaredConstructor().newInstance()
        val generator = generatorFactory.createGraphProducer(sourceGraph.loadGraph(), seed, params)
        return generator.produceComputed()
    }

}
