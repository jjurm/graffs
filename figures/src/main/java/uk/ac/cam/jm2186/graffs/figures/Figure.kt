package uk.ac.cam.jm2186.graffs.figures

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Figure(
    val name: String,
    val width: String = "",
    val height: String = "",
    val caption: String
)
