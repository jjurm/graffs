package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.versionOption
import uk.ac.cam.jm2186.graffs.BuildConfig
import java.io.File
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

        eagerOption(names = listOf("-l", "--license"), help = "Show the license") {
            throw PrintMessage(
                """Framework for Empirical Analysis of Graph Metric Robustness
Copyright (C) 2020  Juraj Micko

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
"""
            )
        }
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
