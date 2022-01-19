package maia.util

/*
 * Utilities for working with throwables.
 */

/**
 * Ensures some [condition] is true before executing the given [block].
 *
 * @param condition The condition to check.
 * @param ifNot The exception-generating function to call if the condition is not true.
 * @param block The task to do only if the condition is true.
 */
inline fun <R> ensure(
    condition: Boolean,
    crossinline ifNot: () -> Nothing = { throw Exception() },
    block: () -> R
): R {
    if (!condition) ifNot()
    return block()
}

