package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.options.versionOption
import uk.ac.cam.jm2186.graffs.BuildConfig
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Graffs : CliktCommand(
    name = "graffs",
    printHelpOnEmptyArgs = true,
    help = "Tool for evaluating Graph Metric Robustness",
    autoCompleteEnvvar = "_GRAFFS_COMPLETE",
    epilog = """
        ```
        Examples:
        > graffs db drop
        > graffs dataset list
        > graffs dataset load social-network
        > graffs generator create --help
        > graffs generator create --name g1 -n 10 --method removing-edges --params 0.05
        > graffs experiment create --help
        > graffs experiment create --name e1 --datasets test,social-network --generator g1 --metrics Degree,PageRank,Betweenness --robustnessMeasures RankInstability
        > graffs experiment run --name e1
        ```
    """.trimIndent()
) {

    init {
        context {
            helpFormatter = CliktHelpFormatter(
                requiredOptionMarker = "*",
                showDefaultValues = true
            )
        }

        subcommands(
            DatasetSubcommand(),
            MetricSubcommand(),
            GeneratorSubcommand(),
            ExperimentSubcommand(),
            PlotSubcommand(),

            DatabaseSubcommand()
        )
    }

    init {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        versionOption(
            names = setOf("-v", "--version"),
            version = BuildConfig.VERSION,
            message = { "${BuildConfig.NAME} version $it\nBuilt ${formatter.format(BuildConfig.BUILD_DATE)}" }
        )
    }

    override fun run() {
    }
}

fun main(args: Array<String>) = Graffs().main(args)
