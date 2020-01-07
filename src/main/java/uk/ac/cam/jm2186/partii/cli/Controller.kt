package uk.ac.cam.jm2186.partii.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.*
import uk.ac.cam.jm2186.BuildConfig
import uk.ac.cam.jm2186.partii.graph.GraphProducerFactory
import uk.ac.cam.jm2186.partii.graph.RemovingEdgesGraphProducer
import uk.ac.cam.jm2186.partii.pipeline.ExperimentGeneratorHelper
import uk.ac.cam.jm2186.partii.storage.GraphDataset

class Controller : CliktCommand(printHelpOnEmptyArgs = true) {

    init {
        subcommands(
            LoadDatasetsCommand(),

            GenerateGraphsCommand(),
            ExecuteExperimentCommand()
        )
        versionOption(version = BuildConfig.VERSION, message = { "${BuildConfig.NAME} version $it" })
    }

    class GenerateGraphsCommand : CliktCommand(
        name = "generate-graphs",
        help = "Generate random graphs from source dataset"
    ) {
        val n by option("-n", help = "number of graphs to generate").int().default(10)
        val dataset by option(help = "source dataset to generate graphs from").enum(GraphDataset::id)
            .default(GraphDataset.SocialNetwork)
        val generator by option(help = "algorithm to generate graphs").choice<Class<out GraphProducerFactory>>(
            "removing-edges" to RemovingEdgesGraphProducer.Factory::class.java
        ).default(RemovingEdgesGraphProducer.Factory::class.java)
        val params by option(help = "parameters to pass to the generator").double().multiple(default = listOf(0.05))
        val seed by option(help = "optional seed to the generator").long()

        override fun run() {
            val helper = ExperimentGeneratorHelper()
            helper.generateNGraphsFromDataset(dataset, n, generator, params, seed)
        }
    }

    override fun run() = Unit
}

fun main(args: Array<String>) = Controller().main(args)
