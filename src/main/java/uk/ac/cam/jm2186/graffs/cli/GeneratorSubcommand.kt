package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.ac.cam.jm2186.graffs.storage.getAllEntities
import uk.ac.cam.jm2186.graffs.storage.model.*
import uk.ac.cam.jm2186.graffs.storage.inTransaction
import java.util.*

class GeneratorSubcommand : NoRunCliktCommand(
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

        val name by graphGenerator_name()
        val n by graphGenerator_n()
        val method by graphGenerator_method()
        val params by graphGenerator_params()
        val seed by graphGenerator_seed()

        override fun run0() {
            val seed = seed ?: Random().nextLong()
            val generator = GraphGenerator(name, n = n, method = method, params = params, seed = seed)

            hibernate.beginTransaction()
            hibernate.save(generator)
            hibernate.transaction.commit()
        }
    }

    class RemoveSub : AbstractHibernateCommand(
        name = "remove",
        help = "Remove a graph generator"
    ) {
        val name by graphGenerator_name()

        override fun run0() {
            val generator = hibernate.load(GraphGenerator::class.java, name)
            hibernate.inTransaction { delete(generator) }
        }
    }

}
