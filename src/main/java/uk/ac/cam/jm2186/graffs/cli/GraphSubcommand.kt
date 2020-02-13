package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import org.graphstream.graph.Graph
import uk.ac.cam.jm2186.graffs.graph.GraphProducerFactory
import uk.ac.cam.jm2186.graffs.graph.RemovingEdgesGraphProducer
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.HibernateHelper
import uk.ac.cam.jm2186.graffs.storage.model.DistortedGraph
import java.util.*

class GraphSubcommand : NoRunCliktCommand(
    name = "graph",
    help = "Access generated graphs",
    printHelpOnEmptyArgs = true
) {

    init {
        subcommands(
            ShowCommand(),
            GenerateGraphsCommand(),
            VisualiseCommand()
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
            val generatedGraph = DistortedGraph(
                sourceGraph = graphDataset,
                generator = graphProducerFactory,
                seed = random.nextLong(),
                params = params
            )
            session.save(generatedGraph)
        }
        session.transaction.commit()
    }

    inner class ShowCommand : CliktCommand(
        name = "show",
        help = "Print summary of generated graphs in the database"
    ) {
        override fun run() {
            val count = this@GraphSubcommand.sessionFactory.openSession().use { session ->
                val builder = session.criteriaBuilder
                val criteria = builder.createQuery(DistortedGraph::class.java)
                criteria.from(DistortedGraph::class.java)
                session.createQuery(criteria).list().size
            }
            println("There are $count generated graphs")
        }
    }

    class GenerateOptionGroup : OptionGroup() {
        val n by option("-n", help = "number of graphs to generate").int().required()
        val dataset by option(help = "source dataset to generate graphs from").convert { GraphDataset(it) }.required()
        val generator by option(help = "algorithm to generate graphs").choice<Class<out GraphProducerFactory>>(
            "removing-edges" to RemovingEdgesGraphProducer.Factory::class.java
        ).default(RemovingEdgesGraphProducer.Factory::class.java)
        val params by option(help = "parameters to pass to the generator").double().multiple(default = listOf(0.05))
        val seed by option(help = "optional seed to the generator").long()
    }

    inner class GenerateGraphsCommand : CliktCommand(
        name = "generate",
        help = "Generate random graphs from a source dataset"
    ) {

        val generateOptions by GenerateOptionGroup().cooccurring()

        override fun run() {
            (generateOptions ?: throw PrintHelpMessage(this))
                .apply {
                    this@GraphSubcommand.generateNGraphsFromDataset(dataset, n, generator, params, seed)
                }
        }
    }

    inner class VisualiseCommand : AbstractVisualiseSubcommand() {
        val index by argument(
            "<index>", help = "Index of the generated graph in the database to visualise"
        ).long().default(1)

        override fun getGraph(): Graph {
            this@GraphSubcommand.sessionFactory.openSession().use { session ->
                val graph: DistortedGraph? = session.find(DistortedGraph::class.java, index)

                if (graph == null) {
                    throw BadParameterValue(
                        "Generated graph with index $index not found",
                        paramName = VisualiseCommand::index.name
                    )
                } else {
                    return graph.produceGenerated()
                }
            }
        }
    }

}
