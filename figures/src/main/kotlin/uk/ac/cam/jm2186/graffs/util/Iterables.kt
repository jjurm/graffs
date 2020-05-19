package uk.ac.cam.jm2186.graffs.util

/**
 * Removes all elements from this [MutableIterator] that match the given [predicate].
 *
 * @return `true` if any element was removed from this iterator, or `false` when no elements were removed and the
 * underlying collection was not modified.
 */
fun <T> MutableIterator<T>.removeAll(predicate: (T) -> Boolean): Boolean {
    var result = false
    with(iterator()) {
        while (hasNext())
            if (!predicate(next())) {
                remove()
                result = true
            }
    }
    return result
}
