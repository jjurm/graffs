package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import org.apache.spark.api.java.JavaSparkContext
import org.hibernate.Session
import uk.ac.cam.jm2186.graffs.SparkHelper
import uk.ac.cam.jm2186.graffs.metric.MetricType
import uk.ac.cam.jm2186.graffs.storage.HibernateHelper
import uk.ac.cam.jm2186.graffs.storage.model.GeneratedGraph
import uk.ac.cam.jm2186.graffs.storage.model.MetricExperiment
import uk.ac.cam.jm2186.graffs.storage.model.MetricExperimentId

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
            val toCompute = mutableListOf<MetricExperimentId>()
            /*val seq: Seq<Pair<MetricType, GeneratedGraph>> =
                JavaConverters.iterableAsScalaIterableConverter(toCompute).asScala().toSeq()*/

            println("Connecting to database")
            hibernate.openSession().use { hibernate ->
                println("Generating experiments")
                MetricType.values().forEach { metric ->
                    hibernate.getAllGeneratedGraphs().forEach { graph ->
                        val id = MetricExperimentId(metric.id, graph)
                        if (!hibernate.byId(MetricExperiment::class.java).loadOptional(id).isPresent) {
                            toCompute.add(id)
                        }
                    }
                }
            }

            println("Initialising spark")

            val jsc = JavaSparkContext(spark.sparkContext())
            val slices = 128

            val dataSet = jsc.parallelize(toCompute, slices)

            println("Running computation in parallel (${toCompute.size} metric evaluations)")

            val future = dataSet.map { (metricId, generatedGraph) ->
                // TODO allow specifying metric params
                val metricType = MetricType.byId(metricId)
                val metric = metricType.metricFactory.createMetric(emptyList())
                val graph = generatedGraph.produceGenerated()
                val (value, graphValues) = metric.evaluate(graph)
                println("- executed ${metricType.id} on ${generatedGraph.sourceGraph.id} with seed ${generatedGraph.seed}")
                return@map MetricExperiment(metricType.id, generatedGraph, value, graphValues)
            }

            // Compute metrics on the cluster
            val computedExperiments = future.collect()

            println("Storing results in database")

            // Store results
            hibernate.openSession().use { hibernate ->
                println("=== Results:")
                hibernate.beginTransaction()
                computedExperiments.forEach { experiment ->
                    println("Experiment. Metric: ${experiment.metricId}. Result: ${"%.3f".format(experiment.value)}")
                    hibernate.saveOrUpdate(experiment)
                }
                hibernate.transaction.commit()
            }

            spark.stop()
            hibernate.close()
        }

        private fun Session.getAllGeneratedGraphs(): Iterable<GeneratedGraph> {
            val builder = this.criteriaBuilder

            val criteria = builder.createQuery(GeneratedGraph::class.java)
            criteria.from(GeneratedGraph::class.java)
            return this.createQuery(criteria).list()
        }
    }

}
