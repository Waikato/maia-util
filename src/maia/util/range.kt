package maia.util

/**
 * Gets the size of a closed range of doubles.
 */
fun ClosedRange<Double>.size(): Double {
    return endInclusive - start
}

/**
 * Checks if a value is within a given range.
 *
 * @param index
 *          The value to check.
 * @param upperBound
 *          The maximum valid value.
 * @param lowerBound
 *          The minimum valid value.
 * @param closedUpperBound
 *          Whether [upperBound] itself should be considered valid.
 * @param closedLowerBound
 *          Whether [lowerBound] itself should be considered valid.
 *
 * @return True if the value is valid for the specified range, false if not.
 */
inline fun indexInRange(
    index: Int,
    upperBound: Int,
    lowerBound: Int,
    closedUpperBound: Boolean = false,
    closedLowerBound: Boolean = true
): Boolean {
    return (index > lowerBound || (closedLowerBound && index == lowerBound)) && (index < upperBound || (closedUpperBound && index == upperBound))
}

/**
 * Checks if an index is in the valid range for element access.
 *
 * @param index
 *          The index to check.
 * @param size
 *          The size of the structure being accessed by [index].
 * @param includeSize
 *          Whether to include [index] == [size] as valid (e.g.
 *          for insertion index checks).
 *
 * @return  True if [index] is at least 0, and less than [size],
 *          or equal to [size] if size is included.
 */
inline fun indexInRange(
    index: Int,
    size: Int,
    includeSize: Boolean = false
): Boolean {
    return index >= 0 && (index < size || (includeSize && index == size))
}

/**
 * Performs a given action only if the [fromIndex]/[toIndex]
 * are valid sub-list indices.
 *
 * @param fromIndex [List.subList]
 * @param toIndex [List.subList]
 * @param size The size of the list.
 * @param message The message to include with the exception if [fromIndex] > [toIndex].
 * @param block The action to perform if the indices are valid.
 *
 * @return The result of the [block].
 *
 * @throws IndexOutOfBoundsException If [fromIndex]/[toIndex] are not valid sub-list indices.
 * @throws IllegalArgumentException If [fromIndex] > [toIndex].
 */
inline fun <R> ensureSublistRange(
    fromIndex: Int,
    toIndex: Int,
    size: Int,
    crossinline message: () -> String = { "fromIndex ($fromIndex) can't be greater than toIndex ($toIndex)" },
    crossinline block: () -> R
): R {
    ensureIndexInRange(fromIndex, size, true) {}
    ensureIndexInRange(toIndex, size, true) {}
    if (fromIndex > toIndex)
        throw IllegalArgumentException(message())
    return block()
}

/**
 * Performs the given [block] only if the given [index] is a valid index.
 *
 * @param index The index to check.
 * @param size The size of the structure being accessed by [index].
 * @param includeSize Whether to include [index] == [size] as valid (e.g. for insertion index checks).
 * @param block The action to perform if the [index] is valid.
 *
 * @return The result of the [block].
 *
 * @throws IndexOutOfBoundsException If the index is not in the specified range. Includes [message] as the [IndexOutOfBoundsException.message].
 */
inline fun <R> ensureIndexInRange(
    index: Int,
    size: Int,
    includeSize: Boolean = false,
    crossinline message: () -> String = { indexOutOfBounds(index, size, 0, includeSize) },
    block: () -> R
): R = ensure(
    indexInRange(index, size, includeSize),
    { throw IndexOutOfBoundsException(message()) },
    block
)

/**
 * Throws a generic [IndexOutOfBoundsException].
 *
 * @param index
 *          The index that is out-of-bounds.
 *
 * @throws IndexOutOfBoundsException Always.
 */
fun indexOutOfBounds(index: Int): Nothing {
    throw IndexOutOfBoundsException("Index $index is out of bounds")
}

/**
 * Throws an [IndexOutOfBoundsException] including the bounds information.
 *
 * @param index
 *          The index that is out-of-bounds.
 * @param upperBound
 *          The maximum valid value.
 * @param lowerBound
 *          The minimum valid value.
 * @param closedUpperBound
 *          Whether [upperBound] itself should be considered valid.
 * @param closedLowerBound
 *          Whether [lowerBound] itself should be considered valid.
 *
 * @throws IndexOutOfBoundsException Always.
 */
fun indexOutOfBounds(
    index: Int,
    upperBound: Int,
    lowerBound: Int,
    closedUpperBound: Boolean = false,
    closedLowerBound: Boolean = true
): Nothing {
    val upperBracket = if (closedUpperBound) "]" else ")"
    val lowerBracket = if (closedLowerBound) "[" else "("
    throw IndexOutOfBoundsException(
        "Index $index is out of range; must be in $lowerBracket$lowerBound, $upperBound$upperBracket"
    )
}
