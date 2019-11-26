package uk.ac.cam.jm2186.partii.storage.model

import uk.ac.cam.jm2186.partii.metric.MetricFactory
import uk.ac.cam.jm2186.partii.storage.AbstractJpaPersistable
import java.io.Serializable
import javax.persistence.*

@Entity
class MetricEvaluation(
    @Id
    val id: MetricEvaluationId
) : AbstractJpaPersistable<Long>() {

    @Embeddable
    class MetricEvaluationId(
        val metric: Class<out MetricFactory<*>>,
        @ManyToOne(fetch = FetchType.EAGER)
        val graph: GeneratedGraph
    ) : Serializable

    constructor(
        metric: Class<out MetricFactory<*>>,
        graph: GeneratedGraph
    ) : this(MetricEvaluationId(metric, graph))

}
