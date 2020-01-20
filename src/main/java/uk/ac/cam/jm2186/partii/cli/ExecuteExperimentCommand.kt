package uk.ac.cam.jm2186.partii.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import org.apache.spark.api.java.JavaSparkContext
import org.hibernate.Session
import uk.ac.cam.jm2186.partii.SparkHelper
import uk.ac.cam.jm2186.partii.metric.AverageDegreeMetric
import uk.ac.cam.jm2186.partii.metric.MetricFactory
import uk.ac.cam.jm2186.partii.storage.HibernateHelper
import uk.ac.cam.jm2186.partii.storage.model.GeneratedGraph
import uk.ac.cam.jm2186.partii.storage.model.MetricExperiment
import uk.ac.cam.jm2186.partii.storage.model.MetricType

class ExecuteExperimentCommand : CliktCommand(
    name = "experiment",
    help = "Execute experiment for given <graph, metric>"
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
            computedExperiments.forEach { experiment ->
                println("Experiment. Metric type: ${experiment.metric.name}. Result: ${"%.3f".format(experiment.value)}")
                hibernate.save(experiment)
            }
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
