package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import org.apache.commons.lang3.StringUtils.leftPad
import org.apache.commons.lang3.StringUtils.rightPad
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.api.java.JavaSparkContext
import org.hibernate.Session
import org.hibernate.query.Query
import uk.ac.cam.jm2186.graffs.SparkHelper
import uk.ac.cam.jm2186.graffs.graph.GraphProducerId
import uk.ac.cam.jm2186.graffs.graph.IdentityGenerator
import uk.ac.cam.jm2186.graffs.metric.Metric
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasure
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureFactory
import uk.ac.cam.jm2186.graffs.robustness.RobustnessMeasureId
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.model.*
import uk.ac.cam.jm2186.graffs.util.TimePerf
import java.util.concurrent.TimeUnit
import kotlin.math.max

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

    inner class ShowCommand : AbstractHibernateCommand(
        name = "show",
        help = "Print summary of experiments in the database"
    ) {
        override fun run0() {
            val builder = hibernate.criteriaBuilder
            val criteria = builder.createQuery(MetricExperiment::class.java)
            criteria.from(MetricExperiment::class.java)
            val count = hibernate.createQuery(criteria).list().size
            println("There are $count executed experiments")
        }
    }

    inner class ExecuteCommand : AbstractHibernateCommand(
        name = "execute",
        help = "Execute experiments, i.e. evaluate metrics on generated graphs"
    ) {

        val tags by option(
            "--tag", "--tags",
            help = "Tags of graphs that this experiment should run on, delimited by comma"
        ).split(",").required()

        private val config by requireObject<Controller.Config>()
        private val spark by SparkHelper.delegate { config.runOnCluster }

        override fun run0() {
            val timePerf = TimePerf()

            val toCompute = mutableListOf<MetricExperimentId>()

            println(timePerf.phase("Preparing experiments"))
            val generatedGraphs = hibernate.getAllGeneratedGraphs(tags)
            Metric.map.keys.forEach { metricId ->
                generatedGraphs.forEach { graph ->
                    val id = MetricExperimentId(metricId, graph)
                    if (!hibernate.byId(MetricExperiment::class.java).loadOptional(id).isPresent) {
                        toCompute.add(id)
                    }
                }
            }

            println(timePerf.phase("Initialising spark"))

            val jsc = JavaSparkContext(spark.sparkContext())
            val slices = jsc.getNumberOfSlices()
            val dataSet = jsc.parallelize(toCompute, slices)

            println(timePerf.phase("Running computation in parallel") + " (${toCompute.size} metric evaluations in $slices partitions)")

            val sMaxTagLength = generatedGraphs.map { it.tag?.length ?: 0 }.max()!!
            val sMaxGraphLength = generatedGraphs.map { it.datasetId.length }.max()!!
            val sMaxMetricLength = Metric.map.keys.map { it.length }.max()!!
            val future = dataSet.map { (metricId, distortedGraph) ->
                // TODO allow specifying metric params
                val metric = Metric.map.getValue(metricId).createMetric(emptyList())
                val graph = distortedGraph.produceGenerated()

                val stopWatch = StopWatch()
                stopWatch.start()
                val (value, graphValues) = metric.evaluate(graph)
                stopWatch.stop()

                val sTag = rightPad(distortedGraph.tag?.let { "`$it`" } ?: "*", sMaxTagLength + 2)
                val sGraph = rightPad(distortedGraph.datasetId, sMaxGraphLength)
                val sSeed = leftPad(distortedGraph.seed.toString(16), 17)
                val sMetric = rightPad(metricId, sMaxMetricLength)
                val sResult = rightPad(if (value == null) "[graph object]" else "%.3f".format(value), 14)
                val sTime = "${stopWatch.getTime(TimeUnit.SECONDS)}s"
                println("- $sTag $sGraph (seed $sSeed) -> $sMetric = $sResult  ($sTime)")
                return@map MetricExperiment(metricId, distortedGraph, stopWatch.time, value, graphValues)
            }

            // Compute metrics on the cluster
            val computedExperiments = future.collect()

            println(timePerf.phase("Storing results in database"))

            // Store results
            hibernate.beginTransaction()
            computedExperiments.forEach { experiment ->
                hibernate.saveOrUpdate(experiment)
            }
            hibernate.transaction.commit()

            println("Timings:")
            timePerf.finish().forEach {
                println("  ${rightPad(it.phase, 35)} : ${it.humanReadableDuration()}")
            }

            spark.stop()
        }

        private fun Session.getAllGeneratedGraphs(tags: List<String>): List<DistortedGraph> {
            val builder = this.criteriaBuilder

            val criteria = builder.createQuery(DistortedGraph::class.java)
            val root = criteria.from(DistortedGraph::class.java)
            criteria.select(root)
                .where(root.get(DistortedGraph_.tag).`in`(tags))
            val distortedGraphs = this.createQuery(criteria).list()

            // now we need to collect each dataset's identity graph
            val datasets = distortedGraphs.map { it.datasetId }.distinct()
            val criteria2 = builder.createQuery(DistortedGraph::class.java)
            val root2 = criteria2.from(DistortedGraph::class.java)
            criteria2.select(root2).where(
                root2.get(DistortedGraph_.datasetId).`in`(datasets),
                builder.equal(root2.get<GraphProducerId>(DistortedGraph_.generator), IdentityGenerator.ID)
            )
            val graphs = this.createQuery(criteria2).list()
            graphs.addAll(distortedGraphs)
            return graphs
        }

        private fun JavaSparkContext.getNumberOfSlices(): Int {
            return max(
                2, max(
                    Runtime.getRuntime().availableProcessors(),
                    sc().executorMemoryStatus.size()
                )
            )
        }
    }

    inner class RobustnessCommand : AbstractHibernateCommand(
        name = "robustness",
        help = "Calculate the robustness measure"
    ) {

        private val tag by option("--tag", help = "Filter distorted graphs by the specified tag").required()
        private val dataset by option("--dataset", help = "Use graphs distorted from this dataset")
            .convert { GraphDataset(it, validate = true) }.required()
        private val metric: MetricId by option("--metric", help = "Metric whose robustness to calculate")
            .choice(*Metric.map.keys.toTypedArray()).required()
        private val robustnessMeasure: Pair<RobustnessMeasureId, RobustnessMeasureFactory>
                by option("--measure", help = "Robustness measure")
                    .choicePairs(RobustnessMeasure.map).required()

        private fun filterMetricExperiments(justIdentityGraph: Boolean): Query<MetricExperiment> {
            val builder = hibernate.criteriaBuilder
            val criteria = builder.createQuery(MetricExperiment::class.java)
            val root = criteria.from(MetricExperiment::class.java)
            val graph = root.get(MetricExperiment_.graph)
            criteria.select(root)
                .where(
                    builder.equal(graph.get(DistortedGraph_.datasetId), dataset.id),
                    builder.equal(root.get(MetricExperiment_.metricId), metric),
                    builder.equal(graph.get<GraphProducerId>(DistortedGraph_.generator), IdentityGenerator.ID)
                        .run { if (justIdentityGraph) this else not() },
                    if (justIdentityGraph) builder.conjunction()
                    else builder.equal(graph.get(DistortedGraph_.tag), tag)
                )
            return hibernate.createQuery(criteria)
        }

        private fun filterMetricExperimentsOfSource(): MetricExperiment =
            filterMetricExperiments(true).singleResult
                ?: throw IllegalStateException("The database contains no entry of evaluated $metric on the source dataset `${dataset.id}`")

        private fun filterMetricExperimentsOfDistorted(): List<MetricExperiment> =
            filterMetricExperiments(false).resultList

        override fun run0() {
            val original = filterMetricExperimentsOfSource()
            val experiments = filterMetricExperimentsOfDistorted()
            val measure = robustnessMeasure.second.get()

            val result = measure.evaluate(original.readValuesGraph(), experiments.map { it.readValuesGraph() })

            println("Robustness of $metric on ${experiments.size} samples from dataset `${dataset.id}` is [$result]")
        }
    }

}
