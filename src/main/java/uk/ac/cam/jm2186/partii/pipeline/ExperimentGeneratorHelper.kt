package uk.ac.cam.jm2186.partii.pipeline

import uk.ac.cam.jm2186.partii.graph.GraphProducerFactory
import uk.ac.cam.jm2186.partii.storage.GraphDataset
import uk.ac.cam.jm2186.partii.storage.HibernateHelper
import uk.ac.cam.jm2186.partii.storage.model.GeneratedGraph
import java.util.*

class ExperimentGeneratorHelper {

    private val sessionFactory = HibernateHelper.getBaseConfiguration().buildSessionFactory()

    fun generateNGraphsFromDataset(
        graphDataset: GraphDataset,
        n: Int,
        graphProducerFactory: Class<out GraphProducerFactory>,
        seed: Long? = null
    ) = sessionFactory.openSession().use { session ->
        val random = Random()
        if (seed != null) random.setSeed(seed)

        session.beginTransaction()
        (0 until n).forEach { _ ->
            val generatedGraph = GeneratedGraph(
                sourceGraph = graphDataset,
                generator = graphProducerFactory,
                seed = random.nextLong(),
                params = emptyList()
            )
            session.save(generatedGraph)
        }
        session.transaction.commit()
    }

    /*fun generateAllNonExistentEvaluations() = sessionFactory.openSession().use { session ->
        val metrics = getAllMetrics()
        val generatedGraphs = getAllGeneratedGraphs()

        session.beginTransaction()
        metrics.forEach { metric ->
            generatedGraphs.forEach { generatedGraph ->
                val evaluation = MetricExperiment(metric, generatedGraph)
                session.saveOrUpdate(evaluation)
            }
        }
        session.transaction.commit()
    }*/



}
