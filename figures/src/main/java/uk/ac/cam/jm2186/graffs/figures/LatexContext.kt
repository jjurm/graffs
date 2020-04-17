package uk.ac.cam.jm2186.graffs.figures

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend

enum class FileType(val extension: String) {
    PNG("png"), PDF("pdf");
}

interface FigureContext {
    fun newTargetFile(type: FileType = FileType.PNG): File

    fun log(text: String)
}

class LatexContext(private val annotation: Figure) : Figures() {
    val figureName get() = annotation.name
    val width get() = annotation.width
    val height get() = annotation.height
    val caption get() = annotation.caption

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
            val gfxArgs = listOf(width * { "width=$it" } + height * { "height=$it" })
                .joinToString(",", "[", "]")
            val gfx = filenames.joinToString("", transform = { (_, file) -> """\includegraphics$gfxArgs{$file}""" })
            return """\begin{figure}
$gfx
\caption{$caption}
\label{fig:$figureName}
\end{figure}
"""
        }

    suspend fun generateFigure(callable: KFunction<*>) {
        try {
            callable.callSuspend(this)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
        println("Generated $figureName")
    }

    suspend fun exportTex(texFiguresDir: File) {
        try {
            withContext(Dispatchers.IO) {
                filenames.forEach { (_, filename) ->
                    FileUtils.copyFile(file(filename), File(texFiguresDir, filename))
                }
                FileUtils.writeStringToFile(File(texFiguresDir, "$figureName.tex"), texCode, Charsets.UTF_8)
            }
            println("Exported $figureName")
        } catch (e: IOException) {
            System.err.println("Cannot export $figureName")
            e.printStackTrace()
        }
    }
}
