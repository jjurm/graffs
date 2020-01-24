package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.ac.cam.jm2186.graffs.metric.MetricType

class MetricSubcommand : NoRunCliktCommand(
    name = "metric",
    printHelpOnEmptyArgs = true,
    help = "Access available metrics"
) {

    init {
        subcommands(ListMetricsCommand())
    }

    class ListMetricsCommand : CliktCommand(name = "list", help = "List available graph metrics") {
        override fun run() {
            MetricType.values().forEach {
                println("- ${it.id}")
            }
        }
    }

}
