package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.CliktCommand

abstract class AbstractCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = ""
) : CliktCommand(
    help, epilog, name, invokeWithoutSubcommand, printHelpOnEmptyArgs, helpTags, autoCompleteEnvvar
) {

    private val setup = mutableListOf<() -> Unit>()
    private val cleanup = mutableListOf<() -> Unit>()

    protected fun addSetupCallback(callback: () -> Unit) = setup.add(callback)
    protected fun addCleanupCallback(callback: () -> Unit) = cleanup.add(callback)

    abstract fun run0()

    final override fun run() {
        setup.onEach { run() }
        run0()
        cleanup.asReversed().onEach { run() }
    }

}
