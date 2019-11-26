package uk.ac.cam.jm2186.partii.storage.model

import uk.ac.cam.jm2186.partii.graph.GraphProducerFactory
import uk.ac.cam.jm2186.partii.storage.AbstractJpaPersistable
import uk.ac.cam.jm2186.partii.storage.GraphDataset
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
) : AbstractJpaPersistable<Long>()
