package uk.ac.cam.jm2186.graffs.figures

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Figure(
    val name: String,
    val figurePos: String = "",
    val gfxArgs: String = "", // trim=left bottom right top, clip
    val caption: String = "",
    val captionPos: CaptionPos = CaptionPos.BOTTOM,
    val beginEndFigure: Boolean = true,
    val wrapfigureArgs: String = "",
    val vspaceAround: String = "",
    val generateTex: Boolean = true,
    val ignore: Boolean = false
)

enum class CaptionPos { TOP, BOTTOM }
