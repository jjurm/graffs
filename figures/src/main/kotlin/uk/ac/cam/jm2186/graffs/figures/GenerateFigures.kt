package uk.ac.cam.jm2186.graffs.figures

import kotlinx.coroutines.*
import java.io.File


val texFiguresDir = File(System.getProperty("graffs.figures_dir", "figures_gen"))

fun generateAndExportAll(args: Array<String>) {
    getAllFigures()
        .filter { args.isEmpty() || it.context.figureName in args }
        .forEach {
            runBlocking {
                it.generate().export()
            }
        }
}

fun main(args: Array<String>) {
    //generateAndExportAll(args)
    runBlocking {
        Figures::diagramDataModelClasses.figure().generate().export()
    }
}
