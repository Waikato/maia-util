package māia.util

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
