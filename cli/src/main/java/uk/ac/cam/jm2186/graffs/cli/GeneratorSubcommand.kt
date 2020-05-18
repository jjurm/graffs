package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.eagerOption
import uk.ac.cam.jm2186.graffs.db.getAllEntities
import uk.ac.cam.jm2186.graffs.db.inTransaction
import uk.ac.cam.jm2186.graffs.graph.gen.RemovingEdgesFlatGenerator
import uk.ac.cam.jm2186.graffs.db.model.*
import uk.ac.cam.jm2186.graffs.db.mustNotExist
import uk.ac.cam.jm2186.graffs.graph.gen.RemovingEdgesSequenceGenerator
import java.util.*

class GeneratorSubcommand : NoOpCliktCommand(
    name = "generator",
    help = "Create or list graph generators"
) {

    init {
        subcommands(
            ListSub(),
            CreateSub(),
            RemoveSub()
        )
    }

    class ListSub : AbstractHibernateCommand(
        name = "list",
        help = "List available graph generators"
    ) {
        override fun run0() {
            hibernate.getAllEntities(GraphGenerator::class.java).forEach {
                println(
                    """- ${it.name}
                        |  n: ${it.n}
                        |  method: ${it.method}
                        |  params: ${it.params}
                        |  seed: ${it.seed}
                        |  
                    """.trimMargin()
                )
            }
        }
    }

    class CreateSub : AbstractHibernateCommand(
        name = "create",
        help = "Create a new graph generator description"
    ) {

        init {
            eagerOption("--sample", help = "Create a sample generator (overriding all options)") {
                createGenerator(
                    GraphGenerator(
                        name = "sampleGenerator",
                        n = 10,
                        method = RemovingEdgesSequenceGenerator.id,
                        params = listOf(0.05),
                        seed = 42L
                    )
                )
                throw PrintMessage("")
            }
        }

        val name by graphGenerator_name()
        val n by graphGenerator_n()
        val method by graphGenerator_method()
        val params by graphGenerator_params()
        val seed by graphGenerator_seed()

        override fun run0() {
            val seed = seed ?: Random().nextLong()
            createGenerator(
                GraphGenerator(name = name, n = n, method = method, params = params, seed = seed)
            )
        }

        fun createGenerator(generator: GraphGenerator) {
            hibernate.mustNotExist<GraphGenerator>(generator.name)
            hibernate.beginTransaction()
            hibernate.save(generator)
            hibernate.transaction.commit()

            println(generator)
        }
    }

    class RemoveSub : CoroutineCommand(
        name = "remove",
        help = "Remove a graph generator"
    ) {
        val name by graphGenerator_name()

        override suspend fun run1() {
            val generator = hibernate.load(GraphGenerator::class.java, name)
            hibernate.inTransaction { delete(generator) }
        }
    }

}
