package uk.ac.cam.jm2186.graffs.figures

import org.apache.commons.io.FileUtils
import uk.ac.cam.jm2186.graffs.figures.FigureType.PNG
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions

val texFiguresDir = File(System.getProperty("graffs.figures_dir", "../partii/figures_gen"))

enum class FigureType {
    PNG
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Figure(val name: String, val type: FigureType = PNG, val caption: String)

interface FigureContext {
    val targetFile: File
}

class Context(val figureName: String, private val type: FigureType, private val caption: String) : Figures() {
    constructor(annotation: Figure) : this(annotation.name, annotation.type, annotation.caption)

    private val filename get() = "$figureName.png"
    override val targetFile get() = File(File("output"), filename)

    val texCode
        get() = when (type) {
            PNG -> """
                \begin{figure}
                \includegraphics[width=10cm]{$filename}
                \caption{$caption}
                \label{fig:$figureName}
                \end{figure}
            """.trimIndent()
        }

    fun generateFigure(callable: FigureCallable) {
        try {
            callable.call(this)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
        println("Generated $figureName")
    }

    fun exportTex(texFiguresDir: File) {
        try {
            FileUtils.copyFile(targetFile, File(texFiguresDir, filename))
            FileUtils.writeStringToFile(File(texFiguresDir, "$figureName.tex"), texCode, Charsets.UTF_8)
            println("Exported $figureName")
        } catch (e: IOException) {
            System.err.println("Cannot export $figureName")
            e.printStackTrace()
        }
    }
}

typealias FigureCallable = KFunction<*>

fun getAllFigures(): List<FigureCallable> = Figures::class.memberFunctions
    .filter { it.annotations.any { it is Figure } }

private fun FigureCallable.getFigureContext() = Context(annotations.singleOrNull { it is Figure }!! as Figure)

fun FigureCallable.generateFigure() = getFigureContext().generateFigure(this)
fun FigureCallable.exportFigure() = getFigureContext().exportTex(texFiguresDir)
fun FigureCallable.generateAndExportFigure() {
    generateFigure()
    exportFigure()
}

fun main() {
    getAllFigures().forEach { it.generateAndExportFigure() }
}
