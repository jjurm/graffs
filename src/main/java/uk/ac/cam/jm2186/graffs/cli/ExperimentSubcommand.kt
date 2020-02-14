package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import org.apache.commons.lang3.StringUtils.leftPad
import org.apache.commons.lang3.StringUtils.rightPad
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.api.java.JavaSparkContext
import org.hibernate.Session
import uk.ac.cam.jm2186.graffs.SparkHelper
import uk.ac.cam.jm2186.graffs.metric.Metric
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasure
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureFactory
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.HibernateHelper
import uk.ac.cam.jm2186.graffs.storage.model.*
import uk.ac.cam.jm2186.graffs.util.TimePerf
import java.util.concurrent.TimeUnit

class ExperimentSubcommand : NoRunCliktCommand(
    name = "experiment",
    help = "Execute experiments (evaluating metrics on generated graphs) and show results"
) {

    init {
        subcommands(
            ShowCommand(),
            ExecuteCommand(),
            RobustnessCommand()
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
            val timePerf = TimePerf()

            val toCompute = mutableListOf<MetricExperimentId>()
            /*val seq: Seq<Pair<MetricType, GeneratedGraph>> =
                JavaConverters.iterableAsScalaIterableConverter(toCompute).asScala().toSeq()*/

            println(timePerf.phase("Connecting to database"))
            val graphIds = hibernate.openSession().use { hibernate ->
                println(timePerf.phase("Generating experiments"))
                val generatedGraphs = hibernate.getAllGeneratedGraphs()
                Metric.map.keys.forEach { metricId ->
                    generatedGraphs.forEach { graph ->
                        val id = MetricExperimentId(metricId, graph)
                        if (!hibernate.byId(MetricExperiment::class.java).loadOptional(id).isPresent) {
                            toCompute.add(id)
                        }
                    }
                }
                return@use generatedGraphs.map { it.datasetId }.distinct()
            }

            println(timePerf.phase("Initialising spark"))

            val jsc = JavaSparkContext(spark.sparkContext())
            val slices = 128

            val dataSet = jsc.parallelize(toCompute, slices)

            println(timePerf.phase("Running computation in parallel") + " (${toCompute.size} metric evaluations)")

            val sMaxGraphLength = graphIds.map { it.length }.max()!!
            val sMaxMetricLength = Metric.map.keys.map { it.length }.max()!!
            val future = dataSet.map { (metricId, distortedGraph) ->
                // TODO allow specifying metric params
                val metric = Metric.map.getValue(metricId).createMetric(emptyList())
                val graph = distortedGraph.produceGenerated()

                val stopWatch = StopWatch()
                stopWatch.start()
                val (value, graphValues) = metric.evaluate(graph)
                stopWatch.stop()

                val sGraph = rightPad(distortedGraph.datasetId, sMaxGraphLength)
                val sSeed = leftPad(distortedGraph.seed.toString(16), 17)
                val sMetric = rightPad(metricId, sMaxMetricLength)
                val sResult = rightPad(if (value == null) "[graph object]" else "%.3f".format(value), 14)
                val sTime = "${stopWatch.getTime(TimeUnit.SECONDS)}s"
                println("- $sGraph (seed $sSeed) -> $sMetric = $sResult  ($sTime)")
                return@map MetricExperiment(metricId, distortedGraph, stopWatch.time, value, graphValues)
            }

            // Compute metrics on the cluster
            val computedExperiments = future.collect()

            println(timePerf.phase("Storing results in database"))

            // Store results
            hibernate.openSession().use { hibernate ->
                hibernate.beginTransaction()
                computedExperiments.forEach { experiment ->
                    hibernate.saveOrUpdate(experiment)
                }
                hibernate.transaction.commit()
            }

            println("Timings:")
            timePerf.finish().forEach {
                println("  ${rightPad(it.phase,35)} : ${it.humanReadableDuration()}")
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

    inner class RobustnessCommand : CliktCommand(
        name = "robustness",
        help = "Calculate the robustness measure"
    ) {

        val dataset by option("--dataset", help = "Use graphs distorted from this dataset")
            .convert { GraphDataset(it, validate = true) }.required()
        val metric: MetricId by option("--metric", help = "Metric whose robustness to calculate")
            .choice(*Metric.map.keys.toTypedArray()).required()
        val robustnessMeasure: Pair<RobustnessMeasureId, RobustnessMeasureFactory>
                by option("--measure", help = "Robustness measure")
                    .choicePairs(RobustnessMeasure.map).required()

        fun filterMetricExperiments() = this@ExperimentSubcommand.sessionFactory.openSession().use { session ->
            val builder = session.criteriaBuilder
            val criteria = builder.createQuery(MetricExperiment::class.java)
            val root = criteria.from(MetricExperiment::class.java)
            criteria.select(root)
                .where(
                    builder.and(
                        builder.equal(
                            root.get<DistortedGraph>(MetricExperiment_.graph).get(DistortedGraph_.datasetId),
                            dataset.id
                        ),
                        builder.equal(root.get<MetricId>(MetricExperiment_.metricId), metric)
                    )
                )
            session.createQuery(criteria).resultList
        }

        override fun run() {
            val experiments: List<MetricExperiment> = filterMetricExperiments()
            val measure = robustnessMeasure.second.get()

            val originalGraph = dataset.loadGraph()
            val result = measure.evaluate(originalGraph , experiments.map { it.readValuesGraph() })

            println("Robustness of $metric on ${experiments.size} samples from dataset `${dataset.id}` is [$result]")
        }
    }

}
