package uk.ac.cam.jm2186.graffs.cli

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

abstract class CoroutineCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = ""
) : AbstractHibernateCommand(
    help, epilog, name, invokeWithoutSubcommand, printHelpOnEmptyArgs, helpTags, autoCompleteEnvvar
) {

    abstract suspend fun run1()

    final override fun run0() {
        runBlocking(Dispatchers.Default) {
            run1()
        }
    }

}
