package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.alg.giantComponent
import uk.ac.cam.jm2186.graffs.graph.hasWeights
import uk.ac.cam.jm2186.graffs.graph.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.graph.storage.getAvailableDatasetsChecked
import java.io.BufferedInputStream
import java.io.File
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream


class DatasetSubcommand : NoOpCliktCommand(
    name = "dataset",
    printHelpOnEmptyArgs = true,
    help = """
        Access available datasets
        
        Datasets are stored in the `${GraphDataset.DATASET_DIRECTORY_NAME}` folder, each dataset in a subfolder.
        Supported files are:
        ```
        - edges.txt (or *.txt) with lines describing edges with optional weight in the format: NODE1 NODE2 [WEIGHT]
        - *.RData with an R dataframe object named *.df
        ```
    """.trimIndent()
) {

    init {
        subcommands(
            ListDatasetsCommand(),
            LoadDatasetsCommand(),
            DownloadDemoDatasetsCommand(),
            VisualiseCommand()
        )
    }

    class ListDatasetsCommand : CliktCommand(
        name = "list",
        help = "List all datasets available in the `${GraphDataset.DATASET_DIRECTORY_NAME}` directory"
    ) {
        override fun run() {
            GraphDataset.getAvailableDatasetsChecked().forEach { dataset ->
                println()
                println("- ${dataset.id}")
                dataset.loadInfo()?.let { println(it.trimEnd().prependIndent("  ")) }
            }
        }
    }

    class LoadDatasetsCommand : CliktCommand(
        name = "load",
        help = "Check if datasets can be loaded from the `${GraphDataset.DATASET_DIRECTORY_NAME}` directory"
    ) {

        val datasets by argument(
            "datasets",
            help = "Datasets to load. Leave empty to load all present datasets"
        ).convert { GraphDataset(it, validate = true) }.multiple(required = false)

        override fun run() {
            val toLoad: List<GraphDataset>? = if (datasets.isNotEmpty()) datasets
            else GraphDataset.getAvailableDatasetsChecked()

            toLoad?.forEach { dataset ->
                val graph = dataset.loadGraph()
                val scored = if (graph.hasWeights()) "scored" else "unscored"
                println("- ${dataset.id} has ${graph.nodeCount} nodes, ${graph.edgeCount} edges ($scored)")
            }
        }

    }

    class DownloadDemoDatasetsCommand : AbstractCommand(
        name = "download-demos", help = """Download datasets for demonstration
            |
            |List of available datasets: ${demos.keys.joinToString()}
        """.trimMargin()
    ) {
        companion object {

            private fun download(url: String, file: File) {
                FileUtils.copyURLToFile(URL(url), file, 5_000, 10_000)
            }

            private fun getConnection(url: String): URLConnection {
                val urlObj = URL(url)
                val connection = urlObj.openConnection()
                connection.connectTimeout = 5_000
                connection.readTimeout = 10_000
                return connection
            }

            private fun downloadGzip(url: String, file: File) {
                val connection = getConnection(url)
                FileUtils.copyInputStreamToFile(GZIPInputStream(connection.getInputStream()), file)
            }

            private fun downloadKoblenz(dir: File, url: String) {
                val connection = getConnection(url)
                TarArchiveInputStream(
                    BZip2CompressorInputStream(
                        BufferedInputStream(
                            connection.getInputStream()
                        )
                    )
                ).use { tarInput ->
                    while (true) {
                        val entry = tarInput.nextEntry ?: break
                        var flatFilename = File(entry.name).name.substringBefore(".") + ".txt"
                        if (flatFilename == "out.txt") flatFilename = "edges.txt"
                        val outputFile = File(dir, flatFilename)
                        if (!entry.isDirectory) {
                            outputFile.outputStream().use { outputStream ->
                                IOUtils.copy(tarInput, outputStream)
                            }
                        }
                    }
                }
            }

            private fun info(dir: File, info: String) {
                val infoFile = File(dir, "info.txt")
                FileUtils.writeStringToFile(infoFile, info, Charset.defaultCharset())
            }

            private val demos: Map<String, (dir: File) -> Unit> = mapOf(
                "test" to { dir ->
                    val url = "https://github.com/jjurm/graffs/raw/master/data/test/edges.txt"
                    download(url, File(dir, "edges.txt"))
                    info(dir, "Test dataset")
                },
                "ecoli" to { dir ->
                    val url =
                        "https://github.com/lbozhilova/measuring_rank_robustness/raw/master/string_ecoli_data.RData"
                    download(url, File(dir, "string_ecoli_data.RData"))
                    info(dir, "Escherichia coli (ECOLI), STRING")
                },
                "pvivax" to { dir ->
                    val url =
                        "https://github.com/lbozhilova/measuring_rank_robustness/raw/master/string_pvivax_data.RData"
                    download(url, File(dir, "string_pvivax_data.RData"))
                    info(dir, "Plasmodium vivax (PVX), STRING")
                },
                "yeast" to { dir ->
                    val url =
                        "https://github.com/lbozhilova/measuring_rank_robustness/raw/master/string_yeast_data.RData"
                    download(url, File(dir, "string_yeast_data.RData"))
                    info(dir, "Saccharomyces cerevisiae (YEAST), STRING")
                },
                "facebook" to { dir ->
                    downloadGzip("https://snap.stanford.edu/data/facebook_combined.txt.gz", File(dir, "edges.txt"))
                    info(dir, "Social circles: Facebook\nhttps://snap.stanford.edu/data/ego-Facebook.html")
                },
                "citation" to { dir ->
                    downloadGzip("https://snap.stanford.edu/data/cit-HepTh.txt.gz", File(dir, "edges.txt"))
                    info(
                        dir,
                        "High-energy physics theory citation network\nhttps://snap.stanford.edu/data/cit-HepTh.html"
                    )
                },
                "collab" to { dir ->
                    downloadGzip("https://snap.stanford.edu/data/ca-GrQc.txt.gz", File(dir, "edges.txt"))
                    info(
                        dir,
                        "Collaboration network of Arxiv General Relativity category\nhttps://snap.stanford.edu/data/ca-GrQc.html"
                    )
                },
                "internet" to { dir ->
                    downloadKoblenz(dir, "http://konect.uni-koblenz.de/downloads/tsv/topology.tar.bz2")
                    info(dir, "Internet topology network\nhttp://konect.uni-koblenz.de/networks/topology")
                },
                "airports" to { dir ->
                    downloadKoblenz(dir, "http://konect.uni-koblenz.de/downloads/tsv/opsahl-usairport.tar.bz2")
                    info(
                        dir,
                        "Airport–airport flights in the US in 2010\nhttp://konect.uni-koblenz.de/networks/opsahl-usairport"
                    )
                }
            )
        }

        override fun run0() {
            val datasetDir = GraphDataset.datasetDirectory
            Runtime.getRuntime().addShutdownHook(Thread {
                demos.keys.forEach { id ->
                    FileUtils.deleteQuietly(File(datasetDir, ".$id.temp"))
                }
            })
            demos.forEach { (id, lambda) ->
                val tempDir = File(datasetDir, ".$id.temp")
                val targetDir = File(datasetDir, id)
                print("$id: ...")
                if (targetDir.isDirectory) {
                    println("already exists")
                } else {
                    tempDir.mkdirs()
                    lambda(tempDir)
                    tempDir.renameTo(targetDir)
                    println("done")
                }
            }
        }
    }

    class VisualiseCommand : AbstractCommand(
        name = "viz", help = "Visualise graph"
    ) {

        val dataset by argument(
            help = "Dataset to visualise"
        ).convert { GraphDataset(it) }

        val giantComponent by option(
            "-g", "--giant",
            help = "Vizualise only the biggest component of the graph"
        ).flag()

        val outputFile by option(
            "-o", "--output",
            help = "Take a screenshot and save it to the given file", metavar = "FILE"
        ).file(mustExist = false, canBeDir = false)

        private fun getGraph(): Graph {
            try {
                return dataset.loadGraph().run {
                    if (giantComponent) giantComponent() else this
                }
            } catch (e: IllegalArgumentException) {
                throw BadParameterValue(e.message ?: "Could not load dataset", ::dataset.name)
            }
        }

        override fun run0() {
            val visualiser = GraphVisualiser(getGraph())
            val outputFile = outputFile
            if (outputFile == null) {
                visualiser.display()
            } else {
                visualiser.screenshot(outputFile)
            }
        }
    }
}
