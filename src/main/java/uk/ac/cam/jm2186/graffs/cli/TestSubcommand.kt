package uk.ac.cam.jm2186.graffs.cli

import com.github.ajalt.clikt.core.NoRunCliktCommand

class
TestSubcommand : NoRunCliktCommand(
    name = "test"
) {

    override fun run() {
        val dir = System.getProperty("user.dir")
        println("Working directory is ${dir}")
    }

}
