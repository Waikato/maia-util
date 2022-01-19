package maia.util

/**
 * Interface representing a predicate on a value.
 *
 * @param T
 *          The type of value the predicate is for.
 */
fun interface Predicate<T> {
    /**
     * Tests the validity of the predicate on a value.
     *
     * @param value
     *          The value to test.
     * @return
     *          The validity of the predicate.
     */
    fun test(value: T): Boolean
}

operator fun <T> Predicate<T>.not(): Predicate<T> {
    return Predicate { !this@not.test(it) }
}

operator fun <T> Predicate<T>.times(other : Predicate<T>): Predicate<T> {
    return this and other
}

operator fun <T> Predicate<T>.plus(other : Predicate<T>): Predicate<T> {
    return this or other
}

infix fun <T> Predicate<T>.and(other: Predicate<T>): Predicate<T> {
    return Predicate { this@and.test(it) && other.test(it) }
}

infix fun <T> Predicate<T>.or(other: Predicate<T>): Predicate<T> {
    return Predicate { this@or.test(it) || other.test(it) }
}

/**
 * Returns whether the given predicate holds for all items in the given
 * iterable. Short-circuits if any item fails the predicate.
 *
 * @param items
 *          The items to check.
 * @param predicate
 *          The predicate to apply to each item.
 * @return
 *          True if all items meet the predicate.
 */
inline fun <T> all(items: Iterable<T>, crossinline predicate: (T) -> Boolean): Boolean {
    for (item in items) {
        if (!predicate(item)) return false
    }
    return true
}

/**
 * Returns whether the given predicate holds for any item in the given
 * iterable. Short-circuits if any item meets the predicate.
 *
 * @param items
 *          The items to check.
 * @param predicate
 *          The predicate to apply to each item.
 * @return
 *          True if any item meet the predicate.
 */
inline fun <T> any(items: Iterable<T>, crossinline predicate: (T) -> Boolean): Boolean {
    for (item in items) {
        if (predicate(item)) return true
    }
    return false
}
