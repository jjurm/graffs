package uk.ac.cam.jm2186.partii.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import org.apache.spark.api.java.JavaSparkContext
import org.hibernate.Session
import uk.ac.cam.jm2186.partii.SparkHelper
import uk.ac.cam.jm2186.partii.metric.AverageDegreeMetric
import uk.ac.cam.jm2186.partii.metric.MetricFactory
import uk.ac.cam.jm2186.partii.storage.HibernateHelper
import uk.ac.cam.jm2186.partii.storage.model.GeneratedGraph
import uk.ac.cam.jm2186.partii.storage.model.MetricExperiment
import uk.ac.cam.jm2186.partii.storage.model.MetricType

class ExperimentSubcommand : NoRunCliktCommand(
    name = "experiment",
    help = "Execute experiments (evaluating metrics on generated graphs) and show results"
) {

    init {
        subcommands(
            ShowCommand(),
            ExecuteCommand()
        )
    }

    private val sessionFactory by HibernateHelper.delegate()

    inner class ShowCommand : CliktCommand(
        name = "show",
        help = "Print summary of experiments in the database"
    ) {
        override fun run() {
            val count = this@ExperimentSubcommand.sessionFactory.openSession().use { session ->
                val builder = session.criteriaBuilder
                val criteria = builder.createQuery(MetricExperiment::class.java)
                criteria.from(MetricExperiment::class.java)
                session.createQuery(criteria).list().size
            }
            println("There are $count executed experiments")
        }
    }

    inner class ExecuteCommand : CliktCommand(
        name = "execute",
        help = "Execute experiments, i.e. evaluate metrics on generated graphs"
    ) {

        val config by requireObject<Controller.Config>()

        val spark by SparkHelper.delegate { config.runOnCluster }
        val hibernate by HibernateHelper.delegate()

        override fun run() {
            val toCompute = mutableListOf<Pair<MetricType, GeneratedGraph>>()
            /*val seq: Seq<Pair<MetricType, GeneratedGraph>> =
                JavaConverters.iterableAsScalaIterableConverter(toCompute).asScala().toSeq()*/

            hibernate.openSession().use { hibernate ->
                getAllMetrics().forEach { metric ->
                    hibernate.getAllGeneratedGraphs().forEach { graph ->
                        toCompute.add(metric to graph)
                    }
                }
            }

            val jsc = JavaSparkContext(spark.sparkContext())
            val slices = 128

            val dataSet = jsc.parallelize(toCompute, slices)

            val future = dataSet.map { (metricType, generatedGraph) ->
                val metricFactory: MetricFactory<*> = metricType.getDeclaredConstructor().newInstance()
                val metric = metricFactory.createMetric(emptyList())
                val graph = generatedGraph.produceGenerated()
                val result = metric.evaluate(graph)
                return@map MetricExperiment(metricType, generatedGraph, result as Double)
            }

            // Compute metrics on the cluster
            val computedExperiments = future.collect()

            // Store results
            hibernate.openSession().use { hibernate ->
                println("=== Results:")
                hibernate.beginTransaction()
                computedExperiments.forEach { experiment ->
                    println("Experiment. Metric type: ${experiment.metric.name}. Result: ${"%.3f".format(experiment.value)}")
                    hibernate.save(experiment)
                }
                hibernate.transaction.commit()
            }

            spark.stop()
            hibernate.close()
        }

        private fun getAllMetrics(): Iterable<Class<out MetricFactory<*>>> {
            return listOf(
                AverageDegreeMetric.Factory::class.java
            )
        }

        private fun Session.getAllGeneratedGraphs(): Iterable<GeneratedGraph> {
            val builder = this.criteriaBuilder

            val criteria = builder.createQuery(GeneratedGraph::class.java)
            criteria.from(GeneratedGraph::class.java)
            return this.createQuery(criteria).list()
        }
    }

}
