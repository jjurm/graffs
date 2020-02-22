package uk.ac.cam.jm2186.graffs.storage.model

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.GraphProducerFactory
import uk.ac.cam.jm2186.graffs.storage.AbstractJpaPersistable
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.GraphDatasetId
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType

@Entity
class DistortedGraph(
    val datasetId: GraphDatasetId,
    val generator: Class<out GraphProducerFactory>,
    val seed: Long,
    @ElementCollection(fetch = FetchType.EAGER)
    val params: List<Number>,
    val tag: String?
) : AbstractJpaPersistable<Long>() {

    fun produceGenerated(): Graph {
        val generatorFactory = generator.getDeclaredConstructor().newInstance()
        val graph = GraphDataset(datasetId).loadGraph()
        val generator = generatorFactory.createGraphProducer(graph, seed, params)
        return generator.produceComputed()
    }

}
