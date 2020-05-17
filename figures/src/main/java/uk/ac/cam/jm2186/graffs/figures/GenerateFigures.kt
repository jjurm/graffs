package uk.ac.cam.jm2186.graffs.figures

import kotlinx.coroutines.*
import java.io.File


val texFiguresDir = File(System.getProperty("graffs.figures_dir", "../partii/figures_gen"))

fun CoroutineScope.generateAndExportAll(args: Array<String>) {
    getAllFigures()
        .filter { args.isEmpty() || it.context.figureName in args }
        .forEach {
            launch { it.generate().export() }
        }
}

fun main(args: Array<String>) {
    runBlocking {

        Figures::coverGraphImg.figure().generate().export()
        //generateAndExportAll(args)

    }
}
