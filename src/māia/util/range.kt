package māia.util

/**
 * Gets the size of a closed range of doubles.
 */
fun ClosedRange<Double>.size(): Double {
    return endInclusive - start
}
