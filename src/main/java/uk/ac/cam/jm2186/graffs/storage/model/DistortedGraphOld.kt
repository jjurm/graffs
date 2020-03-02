package uk.ac.cam.jm2186.graffs.storage.model

import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.GraphProducer
import uk.ac.cam.jm2186.graffs.graph.GraphProducerId
import uk.ac.cam.jm2186.graffs.storage.AbstractJpaPersistable
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.GraphDatasetId
import javax.persistence.*

@Entity
class DistortedGraphOld(
    val datasetId: GraphDatasetId,
    val generator: GraphProducerId,
    val seed: Long,
    @ElementCollection(fetch = FetchType.EAGER)
    val params: List<Number>,
    @ManyToOne(fetch = FetchType.EAGER)
    val tag: Tag?
) : AbstractJpaPersistable<Long>() {

    @OneToMany(mappedBy = "graph", cascade = [CascadeType.REMOVE], orphanRemoval = true, fetch = FetchType.LAZY)
    protected var metricExperiments: List<MetricExperiment>? = null

    fun produceGenerated(): Graph {
        val generatorFactory = GraphProducer.map.getValue(generator).getDeclaredConstructor().newInstance()
        val graph = GraphDataset(datasetId).loadGraph()
        val generator = generatorFactory.createGraphProducer(graph, seed, params)
        return generator.produceComputed()
    }

}
