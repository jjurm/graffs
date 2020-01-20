package uk.ac.cam.jm2186.partii.cli

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import uk.ac.cam.jm2186.BuildConfig

class Controller : NoRunCliktCommand(
    name = "gmr",
    printHelpOnEmptyArgs = true
) {

    init {
        subcommands(
            DatasetSubcommand(),
            MetricSubcommand(),
            GraphSubcommand(),

            ExecuteExperimentCommand()
        )
        versionOption(version = BuildConfig.VERSION, message = { "${BuildConfig.NAME} version $it" })
    }

    init {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone( ZoneId.systemDefault() )
        versionOption(
            names = setOf("-v", "--version"),
            version = BuildConfig.VERSION,
            message = { "${BuildConfig.NAME} version $it\nBuilt ${formatter.format(BuildConfig.BUILD_DATE)}" }
        )
    }
}

fun main(args: Array<String>) = Controller().main(args)
