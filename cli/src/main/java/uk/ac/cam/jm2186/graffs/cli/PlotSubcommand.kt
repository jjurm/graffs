package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import express.Express
import express.utils.MediaType
import kotlinx.coroutines.*
import tech.tablesaw.api.DoubleColumn
import tech.tablesaw.plotly.components.*
import tech.tablesaw.plotly.traces.ScatterTrace
import uk.ac.cam.jm2186.graffs.db.getNamedEntity
import uk.ac.cam.jm2186.graffs.db.model.Experiment
import uk.ac.cam.jm2186.graffs.db.model.experiment_name
import uk.ac.cam.jm2186.graffs.db.model.metric_name
import uk.ac.cam.jm2186.graffs.graph.AbstractEdgeThresholdGraphProducer
import uk.ac.cam.jm2186.graffs.graph.getNumberAttribute
import uk.ac.cam.jm2186.graffs.metric.Metric
import uk.ac.cam.jm2186.graffs.metric.MetricId
import uk.ac.cam.jm2186.graffs.robustness.GraphAttributeNodeRanking
import uk.ac.cam.jm2186.graffs.robustness.GraphCollectionMetadata
import uk.ac.cam.jm2186.graffs.robustness.RankContinuityMeasure
import uk.ac.cam.jm2186.graffs.robustness.kSimilarity
import java.util.concurrent.Executors

class PlotSubcommand : NoOpCliktCommand(
    name = "plot",
    help = "Plot results"
) {

    init {
        subcommands(
            RankSimilarityPlot()
        )
    }

    abstract class AbstractPlot(
        name: String? = null,
        help: String = ""
    ) : CoroutineCommand(name = name, help = help) {

        companion object {
            @JvmStatic
            protected val colors = listOf(
                "#04254A", // dark blue
                "#FFC103", // yellow
                "#AE2A36", // red
                "#AA7893"  // beige
            )
        }

        private val experimentName by experiment_name()
        protected val experiment by lazy {
            hibernate.getNamedEntity<Experiment>(experimentName)
        }

        private val metricName: MetricId by metric_name()
        protected val metric by lazy {
            Metric.map.getValue(metricName)
        }

        protected suspend fun Figure.plot() {
            val page = Page.pageBuilder(this, "target").build()
            val pageHtml = page.asJavascript()
            val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
            coroutineScope {
                launch(dispatcher) {
                    val app = Express()
                    app.get("/") { _, res ->
                        res.setContentType(MediaType._html)
                        res.send(pageHtml)
                    }
                    val port = 8888
                    println("Starting a webserver on port $port")
                    app.listen(port)
                }
            }
        }
    }

    class RankSimilarityPlot : AbstractPlot(name = "rank-similarity") {

        private fun GraphAttributeNodeRanking.threshold() = graph.getNumberAttribute(
            AbstractEdgeThresholdGraphProducer.ATTRIBUTE_EDGE_THRESHOLD
        )

        override suspend fun run1() {
            val rankContinuity = RankContinuityMeasure()

            val colorIterator = colors.iterator()
            val traces = coroutineScope {
                experiment.graphCollections.map { graphCollection ->
                    val color = colorIterator.next()
                    async {
                        sessionFactory.openSession().use { session ->
                            hibernate.detach(graphCollection)
                            session.update(graphCollection)

                            val overallRanking =
                                GraphCollectionMetadata(graphCollection, metric, this).getOverallRanking()
                            val consecutiveRankingPairs = rankContinuity.consecutiveRankingPairs(overallRanking)
                            // k chosen according to https://github.com/lbozhilova/measuring_rank_robustness/blob/master/figure_generation.R#L28
                            val k = 0.01

                            val kSimilarities = consecutiveRankingPairs
                                .map { (ranking1, ranking2) ->
                                    async {
                                        val kSimilarity = kSimilarity(k, overallRanking, ranking1, ranking2)
                                        ranking1.threshold() / 1000 to kSimilarity
                                    }
                                }
                                .awaitAll()

                            val colThreshold = DoubleColumn.create("threshold")
                            val colKSimilarity = DoubleColumn.create("ksimilarity")
                            kSimilarities.forEach { (threshold, kSimilarity) ->
                                colThreshold.append(threshold)
                                colKSimilarity.append(kSimilarity)
                            }

                            val trace = ScatterTrace.builder(colThreshold, colKSimilarity)
                                .name(graphCollection.dataset)
                                .mode(ScatterTrace.Mode.LINE)
                                .line(Line.builder().color(color).build())
                                .showLegend(true)
                                .build()

                            trace
                        }
                    }
                }.awaitAll()
            }.toMutableList()

            val layout = Layout
                .builder("Rank similarity of ${metric.id}")
                .xAxis(
                    Axis.builder()
                        .title("Threshold")
                        .range(0.0, 1.02)
                        .build()
                )
                .yAxis(
                    Axis.builder()
                        .title("Similarity")
                        .range(0.0, 1.02)
                        .fixedRange(true)
                        .tickSettings(
                            TickSettings.builder()
                                .arrayTicks(doubleArrayOf(0.0, 0.25, 0.5, 0.75, 1.0))
                                .tickMode(TickSettings.TickMode.ARRAY)
                                .build()
                        )
                        .build()
                )
                .build()

            ScatterTrace.builder(doubleArrayOf(0.0, 1.0), doubleArrayOf(0.9, 0.9))
                .mode(ScatterTrace.Mode.LINE)
                .line(Line.builder().color("grey").dash(Line.Dash.DASH).build())
                .showLegend(false)
                .build()
                .let { traces.add(it) }

            listOf(0.15, 0.4, 0.7, 0.9).forEach { score ->
                ScatterTrace.builder(doubleArrayOf(score, score), doubleArrayOf(0.0, 1.0))
                    .mode(ScatterTrace.Mode.LINE)
                    .line(Line.builder().color("grey").dash(Line.Dash.DOT).width(1.0).build())
                    .showLegend(false)
                    .build()
                    .let { traces.add(it) }
            }

            val figure = Figure(layout, *traces.toTypedArray())
            figure.plot()
        }

    }

}
