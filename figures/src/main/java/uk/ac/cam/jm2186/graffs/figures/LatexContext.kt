package uk.ac.cam.jm2186.graffs.figures

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import uk.ac.cam.jm2186.graffs.figures.CaptionPos.BOTTOM
import uk.ac.cam.jm2186.graffs.figures.CaptionPos.TOP
import uk.ac.cam.jm2186.graffs.figures.FileType.*
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend

enum class FileType(val extension: String) {
    PNG("png"), PDF("pdf"), TEX("tex"), CSV("csv");
}

interface FigureContext {
    fun newTargetFile(type: FileType = PNG): File

    fun log(text: String)
}

class LatexContext(private val annotation: Figure) : Figures() {
    val figureName get() = annotation.name
    val figurePos get() = annotation.figurePos
    val gfxArgs get() = annotation.gfxArgs
    val caption get() = annotation.caption
    val captionPos get() = annotation.captionPos
    val generateTex get() = annotation.generateTex
    val ignore get() = annotation.ignore

    private fun filename(index: Int, fileType: FileType) =
        "$figureName${if (index == 0) "" else (index + 1).toString()}.${fileType.extension}"

    private val filenames = mutableListOf<Pair<FileType, String>>()
    private fun file(filename: String) = File(File("output"), filename)

    override fun newTargetFile(type: FileType): File {
        val newFilename = filename(filenames.size, type)
        filenames.add(type to newFilename)
        return file(newFilename)
    }

    override fun log(text: String) = println("[$figureName]: $text")

    private operator fun String?.times(transform: (String) -> String) =
        if (this == null || isEmpty()) "" else transform(this)

    val texCode: String
        get() {
            val sb = StringBuilder()

            val captionAndLabel = """\caption{$caption}
\label{fig:$figureName}"""
            sb.append("""\begin{figure}${figurePos * { "[$it]" }}""" + "\n")
            if (captionPos == TOP) sb.append(captionAndLabel + "\n")

            val files = filenames.joinToString("", transform = { (type, file) ->
                when (type) {
                    PDF, PNG -> """\includegraphics[$gfxArgs]{$file}"""
                    TEX -> file(file).readText()
                    CSV -> ""
                }
            })
            sb.append(files + "\n")

            if (captionPos == BOTTOM) sb.append(captionAndLabel + "\n")
            sb.append("""\end{figure}""" + "\n")

            return sb.toString()
        }

    suspend fun generateFigure(callable: KFunction<*>) {
        if (ignore) return
        try {
            callable.callSuspend(this)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
        println("Generated $figureName")
    }

    suspend fun exportTex(texFiguresDir: File) {
        if (ignore) return
        try {
            withContext(Dispatchers.IO) {
                filenames.forEach { (_, filename) ->
                    FileUtils.copyFile(file(filename), File(texFiguresDir, filename))
                }
                if (generateTex) {
                    FileUtils.writeStringToFile(File(texFiguresDir, "$figureName.tex"), texCode, Charsets.UTF_8)
                }
            }
            println("Exported $figureName")
        } catch (e: IOException) {
            System.err.println("Cannot export $figureName")
            e.printStackTrace()
        }
    }
}
