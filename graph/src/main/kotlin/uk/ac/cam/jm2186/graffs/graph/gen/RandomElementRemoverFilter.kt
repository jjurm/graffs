package uk.ac.cam.jm2186.graffs.graph.gen

import org.graphstream.graph.Element
import org.graphstream.util.Filter
import kotlin.random.Random

internal class RandomElementRemoverFilter<E : Element>(private val deletionRate: Double, seed: Long) : Filter<E> {
    private val random = Random(seed)
    private val decided = HashMap<E, Boolean>()
    override fun isAvailable(e: E): Boolean = decided.computeIfAbsent(e) { random.nextDouble() > deletionRate }
}
