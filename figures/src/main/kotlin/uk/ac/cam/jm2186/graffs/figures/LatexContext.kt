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
    val a get() = annotation
    val figureName get() = a.name

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

            if (a.beginEndFigure) {
                if (a.wrapfigureArgs.isEmpty()) {
                    sb.append("""\begin{figure}${a.figurePos * { "[$it]" }}""" + "\n")
                } else {
                    sb.append("""\begin{wrapfigure}${a.wrapfigureArgs}""" + "\n")
                }
            }

            val captionAndLabel = """\caption{${a.caption}}
\label{fig:$figureName}"""
            if (a.captionPos == TOP) sb.append(captionAndLabel + "\n")

            sb.append(a.vspaceAround * { "\\vspace*{$it}\n" })
            val files = filenames.joinToString("", transform = { (type, file) ->
                when (type) {
                    PDF, PNG -> """\includegraphics[${a.gfxArgs}]{$file}"""
                    TEX -> file(file).readText()
                    CSV -> ""
                }
            })
            sb.append(files + "\n")
            sb.append(a.vspaceAround * { "\\vspace*{$it}\n" })

            if (a.captionPos == BOTTOM) sb.append(captionAndLabel + "\n")
            if (a.beginEndFigure) {
                if (a.wrapfigureArgs.isEmpty()) {
                    sb.append("""\end{figure}""" + "\n")
                } else {
                    sb.append("""\end{wrapfigure}""" + "\n")
                }
            }

            return sb.toString()
        }

    suspend fun generateFigure(callable: KFunction<*>) {
        if (a.ignore) return
        try {
            callable.callSuspend(this)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
        println("Generated $figureName")
    }

    suspend fun exportTex(texFiguresDir: File) {
        if (a.ignore) return
        try {
            withContext(Dispatchers.IO) {
                filenames.forEach { (_, filename) ->
                    FileUtils.copyFile(file(filename), File(texFiguresDir, filename))
                }
                if (a.generateTex) {
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
