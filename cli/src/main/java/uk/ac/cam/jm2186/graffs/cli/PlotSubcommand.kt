package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import express.Express
import express.utils.MediaType
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tech.tablesaw.api.DoubleColumn
import tech.tablesaw.api.StringColumn
import tech.tablesaw.plotly.components.Axis
import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Layout
import tech.tablesaw.plotly.components.Page
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

            val tableMutex = Mutex()
            val colThreshold = DoubleColumn.create("threshold")
            val colKSimilarity = DoubleColumn.create("ksimilarity")
            val colDataset = StringColumn.create("dataset")

            coroutineScope {
                experiment.graphCollections.forEach { (graphDataset, graphCollection) ->
                    launch {
                        val overallRanking = GraphCollectionMetadata(graphCollection, metric, this).getOverallRanking()
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
                        tableMutex.withLock {
                            kSimilarities.forEach { (threshold, kSimilarity) ->
                                colThreshold.append(threshold)
                                colKSimilarity.append(kSimilarity)
                                colDataset.append(graphDataset)
                            }
                        }
                    }
                }
            }

            val layout = Layout
                .builder("Rank similarity of ${metric.id}", colThreshold.name(), colKSimilarity.name())
                .yAxis(
                    Axis.builder()
                        .range(0.0, 1.0)
                        .fixedRange(true)
                        .build()
                )
                .build()
            val trace = ScatterTrace.builder(colThreshold, colKSimilarity)
                .mode(ScatterTrace.Mode.LINE)
                .build()

            val figure = Figure(layout, trace)
            figure.plot()
        }

    }

}
