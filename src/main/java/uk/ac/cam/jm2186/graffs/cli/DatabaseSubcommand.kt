package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import uk.ac.cam.jm2186.graffs.storage.model.Tag


class DatabaseSubcommand : NoRunCliktCommand(
    name = "db",
    help = "Manage the underlying database"
) {

    init {
        subcommands(
            TestCommand(),
            TruncateCommand(),
            ResetCommand()
        )
    }

    inner class TestCommand : AbstractHibernateCommand(name = "test", help = "Test the database connection") {
        override fun run0() {
            if (hibernate.isConnected) println("Successfully connected.")
            else System.err.println("Unsuccessful (see logs above)")
        }
    }

    inner class TruncateCommand : AbstractHibernateCommand(name = "truncate", help = "Truncate parts of the databse") {

        private val tag by option(
            "--tag",
            "--tags",
            help = "Delete all database entries marked with the specified tag(s), delimited by comma"
        ).required()

        override fun run0() {
            hibernate.beginTransaction()

            val tag = hibernate.get(Tag::class.java, tag)
            hibernate.delete(tag)

            /*val builder = hibernate.criteriaBuilder

            val query = builder.createQuery(DistortedGraph::class.java)
            val root = query.from(DistortedGraph::class.java)
            query.where(
                builder.equal(root.get<String>(DistortedGraph_.tag), tag)
            )
            val graphs = hibernate.createQuery(query).list()

            graphs.forEach { hibernate.delete(it) }*/

            hibernate.transaction.commit()
            //println("Deleted ${graphs.size} generated graphs, and related experiments")
        }
    }

    inner class ResetCommand :
        AbstractHibernateCommand(name = "drop", help = "Reset the database schema and contents") {
        override fun run0() {
            hibernate.beginTransaction()
            hibernate.createNativeQuery("DROP ALL OBJECTS").executeUpdate()
            hibernate.transaction.commit()
            println("Database reset.")
        }
    }

}
