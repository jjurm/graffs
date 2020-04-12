package uk.ac.cam.jm2186.graffs.figures

import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions


class FigureProducer(private val callable: KFunction<*>) {
    val context = LatexContext(callable.annotations.singleOrNull { it is Figure }!! as Figure)

    suspend fun generate(): FigureProducer {
        context.generateFigure(callable)
        return this
    }

    suspend fun export() = context.exportTex(texFiguresDir)
}

fun KFunction<*>.figure() = FigureProducer(this)

fun getAllFigures(): List<FigureProducer> = Figures::class.memberFunctions
    .filter { it.annotations.any { it is Figure } }
    .map(::FigureProducer)
