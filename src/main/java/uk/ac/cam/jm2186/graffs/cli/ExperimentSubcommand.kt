package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import org.apache.commons.lang3.StringUtils.leftPad
import org.apache.commons.lang3.StringUtils.rightPad
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.api.java.JavaSparkContext
import org.hibernate.Session
import uk.ac.cam.jm2186.graffs.SparkHelper
import uk.ac.cam.jm2186.graffs.metric.MetricType
import uk.ac.cam.jm2186.graffs.storage.HibernateHelper
import uk.ac.cam.jm2186.graffs.storage.model.DistortedGraph
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
            val metrics = MetricType.values()
            val graphIds = hibernate.openSession().use { hibernate ->
                println("Generating experiments")
                val generatedGraphs = hibernate.getAllGeneratedGraphs()
                metrics.forEach { metric ->
                    generatedGraphs.forEach { graph ->
                        val id = MetricExperimentId(metric.id, graph)
                        if (!hibernate.byId(MetricExperiment::class.java).loadOptional(id).isPresent) {
                            toCompute.add(id)
                        }
                    }
                }
                return@use generatedGraphs.map { it.sourceGraph.id }.distinct()
            }

            println("Initialising spark")

            val jsc = JavaSparkContext(spark.sparkContext())
            val slices = 128

            val dataSet = jsc.parallelize(toCompute, slices)

            println("Running computation in parallel (${toCompute.size} metric evaluations)")

            val sMaxGraphLength = graphIds.map { it.length }.max()!!
            val sMaxMetricLength = metrics.map { it.id.length }.max()!!
            val future = dataSet.map { (metricId, distortedGraph) ->
                // TODO allow specifying metric params
                val metricType = MetricType.byId(metricId)
                val metric = metricType.metricFactory.createMetric(emptyList())
                val graph = distortedGraph.produceGenerated()

                val stopWatch = StopWatch()
                stopWatch.start()
                val (value, graphValues) = metric.evaluate(graph)
                stopWatch.stop()

                val sGraph = rightPad(distortedGraph.sourceGraph.id, sMaxGraphLength)
                val sSeed = leftPad(distortedGraph.seed.toString(16), 17)
                val sMetric = rightPad(metricType.id, sMaxMetricLength)
                val sResult = if (value == null) "[graph object]" else "%.3f".format(value)
                println("- $sGraph (seed $sSeed) -> $sMetric = $sResult")
                return@map MetricExperiment(metricType.id, distortedGraph, stopWatch.time, value, graphValues)
            }

            // Compute metrics on the cluster
            val computedExperiments = future.collect()

            println("Storing results in database")

            // Store results
            hibernate.openSession().use { hibernate ->
                hibernate.beginTransaction()
                computedExperiments.forEach { experiment ->
                    hibernate.saveOrUpdate(experiment)
                }
                hibernate.transaction.commit()
            }

            spark.stop()
            hibernate.close()
        }

        private fun Session.getAllGeneratedGraphs(): List<DistortedGraph> {
            val builder = this.criteriaBuilder

            val criteria = builder.createQuery(DistortedGraph::class.java)
            criteria.from(DistortedGraph::class.java)
            return this.createQuery(criteria).list()
        }
    }

}
