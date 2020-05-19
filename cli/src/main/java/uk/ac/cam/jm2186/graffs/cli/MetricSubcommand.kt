package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.ac.cam.jm2186.graffs.metric.Metrics

class MetricSubcommand : NoOpCliktCommand(
    name = "metric",
    printHelpOnEmptyArgs = true,
    help = "Access available metrics"
) {

    init {
        subcommands(ListMetricsCommand())
    }

    class ListMetricsCommand : CliktCommand(name = "list", help = "List available graph metrics") {
        override fun run() {
            Metrics.map.keys.forEach {
                println("- ${it}")
            }
        }
    }

}
