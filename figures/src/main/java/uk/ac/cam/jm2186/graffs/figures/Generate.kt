package uk.ac.cam.jm2186.graffs.figures

import kotlinx.coroutines.*
import java.io.File


val texFiguresDir = File(System.getProperty("graffs.figures_dir", "../partii/figures_gen"))

fun main(args: Array<String>) {
    runBlocking {
        getAllFigures()
            .filter { args.isEmpty() || it.context.figureName in args }
            .forEach {
                launch { it.generate().export() }
            }
    }
}
