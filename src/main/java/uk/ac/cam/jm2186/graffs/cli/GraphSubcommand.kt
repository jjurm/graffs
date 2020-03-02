package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.subcommands
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
import uk.ac.cam.jm2186.graffs.graph.*
import uk.ac.cam.jm2186.graffs.storage.GraphDataset
import uk.ac.cam.jm2186.graffs.storage.GraphDatasetId
import uk.ac.cam.jm2186.graffs.storage.model.DistortedGraphOld
import uk.ac.cam.jm2186.graffs.storage.model.DistortedGraph_
import uk.ac.cam.jm2186.graffs.storage.model.Tag
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

    inner class ShowCommand : AbstractHibernateCommand(
        name = "show",
        help = "Print summary of generated graphs in the database"
    ) {
        override fun run0() {
            val builder = hibernate.criteriaBuilder
            val criteria = builder.createTupleQuery()
            val root = criteria.from(DistortedGraphOld::class.java)
            criteria.multiselect(root.get(DistortedGraph_.tag), builder.count(root.get(DistortedGraph_.id))).where(
                builder.notEqual(root.get<GraphProducerId>(DistortedGraph_.generator), IdentityGenerator.ID)
            ).groupBy(root.get(DistortedGraph_.tag))
            val results = hibernate.createQuery(criteria).list()
            if (results.isNotEmpty()) {
                results.forEach { tuple ->
                    val tag = tuple.get(0) as String
                    val count = tuple.get(1) as Long
                    println("- $count generated graphs for tag `$tag`")
                }
            } else {
                println("0 generated graphs")
            }
        }
    }

    class GenerateOptionGroup : OptionGroup() {
        val n by option("-n", help = "number of graphs to generate").int().required()
        val dataset by option(help = "source dataset to generate graphs from").convert {
            GraphDataset(it, validate = true)
        }.required()
        val generator by option(help = "algorithm to generate graphs").choice(*GraphProducer.map.keys.toTypedArray())
            .default(RemovingEdgesGenerator.ID)
        val params by option(help = "parameters to pass to the generator, delimited by comma").double().split(delimiter = ",")
            .default(listOf(0.05))
        val seed by option(help = "optional seed to the generator").long()
        val tag by option("--tag", help = "Tags are used to refer to graphs later").required()
    }

    inner class GenerateGraphsCommand : AbstractHibernateCommand(
        name = "generate",
        help = "Generate random graphs from a source dataset"
    ) {

        val generateOptions by GenerateOptionGroup().cooccurring()

        override fun run0() {
            (generateOptions ?: throw PrintHelpMessage(this))
                .apply {
                    this@GenerateGraphsCommand.generateNGraphsFromDataset(dataset, n, generator, params, seed, tag)
                }
        }

        private fun generateNGraphsFromDataset(
            graphDataset: GraphDataset,
            n: Int,
            graphProducerFactory: GraphProducerId,
            params: List<Number>,
            seed: Long?,
            tagName: String
        ) {
            val random = Random()
            if (seed != null) random.setSeed(seed)
            hibernate.beginTransaction()

            // See if the database contains an identity graph for this dataset
            val builder = hibernate.criteriaBuilder
            val criteria = builder.createQuery(DistortedGraphOld::class.java)
            val root = criteria.from(DistortedGraphOld::class.java)
            criteria.select(root)
                .where(
                    builder.equal(root.get<GraphDatasetId>(DistortedGraph_.datasetId), graphDataset.id),
                    builder.equal(root.get<GraphProducerId>(DistortedGraph_.generator), IdentityGenerator.ID)
                )
            if (hibernate.createQuery(criteria).resultList.isEmpty()) {
                val identityGraph = DistortedGraphOld(
                    datasetId = graphDataset.id,
                    generator = IdentityGenerator.ID,
                    seed = 0L,
                    params = emptyList(),
                    tag = null
                )
                hibernate.save(identityGraph)
            }

            val tag = Tag(tagName)
            hibernate.saveOrUpdate(tag)

            // Generate n graphs
            (0 until n).forEach { _ ->
                val generatedGraph = DistortedGraphOld(
                    datasetId = graphDataset.id,
                    generator = graphProducerFactory,
                    seed = random.nextLong(),
                    params = params,
                    tag = tag
                )
                hibernate.save(generatedGraph)
            }

            hibernate.transaction.commit()
        }
    }

    inner class VisualiseCommand : AbstractHibernateCommand(
        name = "viz", help = "Visualise graph"
    ) {
        private val index by argument(
            help = "Index of the generated graph in the database to visualise"
        ).long().default(1)

        private fun getGraph(): Graph {
            val graph = hibernate.find(DistortedGraphOld::class.java, index) ?: throw BadParameterValue(
                "Generated graph with index $index not found",
                paramName = VisualiseCommand::index.name
            )
            return graph.produceGenerated()
        }

        override fun run0() {
            GraphVisualiser().visualise(getGraph())
        }
    }

}
