package uk.ac.cam.jm2186.graffs.cli

import org.hibernate.Session
import org.hibernate.SessionFactory
import uk.ac.cam.jm2186.graffs.db.HibernateHelper


abstract class AbstractHibernateCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = ""
) : AbstractCommand(
    help, epilog, name, invokeWithoutSubcommand, printHelpOnEmptyArgs, helpTags, autoCompleteEnvvar
) {

    protected val sessionFactory: SessionFactory by HibernateHelper.delegate()
    private val sessionLazy: Lazy<Session> = lazy {
        val session = sessionFactory.openSession()
        val url = sessionFactory.properties["hibernate.connection.url"]
        println("Connected to database ($url)")
        session
    }
    protected val hibernate by sessionLazy

    init {
        addCleanupCallback {
            if (sessionLazy.isInitialized()) {
                hibernate.close()
            }
        }
    }

}
