package uk.ac.cam.jm2186.partii.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.ac.cam.jm2186.partii.storage.HibernateHelper


class DatabaseSubcommand : NoRunCliktCommand(
    name = "db",
    help = "Manage the underlying database"
) {

    init {
        subcommands(
            TestCommand(),
            ResetCommand()
        )
    }

    private val sessionFactory by HibernateHelper.delegate()

    inner class TestCommand : CliktCommand(
        name = "test",
        help = "Test the database connection"
    ) {

        override fun run() {
            this@DatabaseSubcommand.sessionFactory.openSession().use { session ->
                if (session.isConnected) println("Successfully connected.")
                else System.err.println("Unsuccessful (see logs above)")
            }
        }
    }

    inner class ResetCommand : CliktCommand(name = "drop", help = "Reset the database schema and contents") {
        override fun run() {
            this@DatabaseSubcommand.sessionFactory
                .openSession()
                .use { session ->
                    session.beginTransaction()
                    session.createNativeQuery("DROP ALL OBJECTS").executeUpdate()
                    session.transaction.commit()
                }
            println("Database reset.")
        }
    }

}
