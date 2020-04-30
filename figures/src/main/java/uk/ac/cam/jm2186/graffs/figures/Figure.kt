package uk.ac.cam.jm2186.graffs.figures

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Figure(
    val name: String,
    val figurePos: String = "",
    val width: String = "",
    val height: String = "",
    val caption: String,
    val captionPos: CaptionPos = CaptionPos.BOTTOM,
    val ignore: Boolean = false
)

enum class CaptionPos { TOP, BOTTOM }
