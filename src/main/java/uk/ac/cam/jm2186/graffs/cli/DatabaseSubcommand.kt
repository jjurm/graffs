package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands


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

    inner class TestCommand : AbstractHibernateCommand(name = "test", help = "Test the database connection") {
        override fun run0() {
            if (hibernate.isConnected) println("Successfully connected.")
            else System.err.println("Unsuccessful (see logs above)")
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
