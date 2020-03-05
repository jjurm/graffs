package uk.ac.cam.jm2186.graffs.cli

import org.hibernate.Session
import org.hibernate.SessionFactory
import uk.ac.cam.jm2186.graffs.storage.HibernateHelper

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

    private val sessionFactory: SessionFactory by HibernateHelper.delegate()
    private val sessionLazy: Lazy<Session> = lazy {
        val session = sessionFactory.openSession()
        println("Conntected to database")
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
