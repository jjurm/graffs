package uk.ac.cam.jm2186.partii.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import uk.ac.cam.jm2186.partii.graph.GraphProducerFactory
import uk.ac.cam.jm2186.partii.graph.RemovingEdgesGraphProducer
import uk.ac.cam.jm2186.partii.storage.GraphDataset
import uk.ac.cam.jm2186.partii.storage.HibernateHelper
import uk.ac.cam.jm2186.partii.storage.model.GeneratedGraph
import java.util.*

class GraphSubcommand : NoRunCliktCommand(
    name = "graph",
    help = "Manage generated graphs",
    printHelpOnEmptyArgs = true
) {

    init {
        subcommands(
            SummaryCommand(),
            GenerateGraphsCommand()
        )
    }

    private val sessionFactory by HibernateHelper.delegate()

    fun generateNGraphsFromDataset(
        graphDataset: GraphDataset,
        n: Int,
        graphProducerFactory: Class<out GraphProducerFactory>,
        params: List<Number>,
        seed: Long? = null
    ) = sessionFactory.openSession().use { session ->
        val random = Random()
        if (seed != null) random.setSeed(seed)

        session.beginTransaction()
        (0 until n).forEach { _ ->
            val generatedGraph = GeneratedGraph(
                sourceGraph = graphDataset,
                generator = graphProducerFactory,
                seed = random.nextLong(),
                params = params
            )
            session.save(generatedGraph)
        }
        session.transaction.commit()
    }

    inner class SummaryCommand() : CliktCommand(
        name = "summary",
        help = "Print summary of generated graphs in the database"
    ) {
        override fun run() {
            val count = this@GraphSubcommand.sessionFactory.openSession().use { session ->
                val builder = session.criteriaBuilder
                val criteria = builder.createQuery(GeneratedGraph::class.java)
                criteria.from(GeneratedGraph::class.java)
                session.createQuery(criteria).list().size
            }
            println("There are $count generated graphs")
        }
    }

    inner class GenerateGraphsCommand : CliktCommand(
        name = "generate",
        help = "Generate random graphs from a source dataset"
    ) {
        val n by option("-n", help = "number of graphs to generate").int().required()
        val dataset by option(help = "source dataset to generate graphs from").convert { GraphDataset(it) }.required()
        val generator by option(help = "algorithm to generate graphs").choice<Class<out GraphProducerFactory>>(
            "removing-edges" to RemovingEdgesGraphProducer.Factory::class.java
        ).default(RemovingEdgesGraphProducer.Factory::class.java)
        val params by option(help = "parameters to pass to the generator").double().multiple(default = listOf(0.05))
        val seed by option(help = "optional seed to the generator").long()

        override fun run() {
            this@GraphSubcommand.generateNGraphsFromDataset(dataset, n, generator, params, seed)
        }
    }

}
