package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import uk.ac.cam.jm2186.BuildConfig
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Controller : CliktCommand(
    name = "graffs",
    printHelpOnEmptyArgs = true,
    help = "Tool for evaluating Graph Metric Robustness",
    epilog = """
        ```
        Examples:
        > graffs db drop
        > graffs dataset list
        > graffs dataset load social-network
        > graffs graph generate --help
        > graffs graph generate -n 10 --dataset social-network --generator removing-edges --params 0.05
        > graffs experiment execute
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
            GraphSubcommand(),
            ExperimentSubcommand(),
            DatabaseSubcommand(),
            TestSubcommand()
        )
    }

    class Config(
        val runOnCluster: Boolean
    )

    val runOnCluster by option("--cluster", help = "Run on cluster. If not specified, runs locally.").flag()

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
        context.findObject { Config(runOnCluster) }
    }
}

fun main(args: Array<String>) = Controller().main(args)
