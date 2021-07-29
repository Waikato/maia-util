package māia.util

/**
 * The range of values an index specifying a bit in an [Int] can take.
 */
val Int.Companion.bitIndexRange : IntRange
    get() = 0 until SIZE_BITS

/**
 * The range of values an index specifying a bit in a [Long] can take.
 */
val Long.Companion.bitIndexRange : IntRange
    get() = 0 until SIZE_BITS

/**
 * Checks an index is within bounds.
 *
 * @param index                         The index to check.
 * @param range                         The range of values the [index] can take.
 * @throws IndexOutOfBoundsException    If the [index] is not in the [range].
 */
fun checkIndex(index : Int, range : IntRange) {
    if (index !in range) throw IndexOutOfBoundsException(index)
}

/**
 * Checks if a bit index is in the range expected for an [Int].
 *
 * @param bitIndex                      The bit-index to check.
 * @throws IndexOutOfBoundsException    If the [bitIndex] is not in the range of
 *                                      allowed values for an [Int].
 */
fun Int.Companion.checkBitIndex(bitIndex : Int) {
    checkIndex(bitIndex, Int.bitIndexRange)
}

/**
 * Checks if a bit index is in the range expected for an [Long].
 *
 * @param bitIndex                      The bit-index to check.
 * @throws IndexOutOfBoundsException    If the [bitIndex] is not in the range of
 *                                      allowed values for an [Long].
 */
fun Long.Companion.checkBitIndex(bitIndex : Int) {
    checkIndex(bitIndex, Long.bitIndexRange)
}

/**
 * Performs the given [block] of code only if the given [index] is in the given
 * [range].
 *
 * @param index                         The index to check.
 * @param range                         The range of values the [index] can take.
 * @param block                         The block to perform if the [index] is in [range].
 * @return                              The result of running the [block].
 * @throws IndexOutOfBoundsException    If the [index] is not in the [range].
 * @param R                             The return-type of the [block].
 */
inline fun <R> withCheckedIndex(index : Int, range : IntRange, block : () -> R) : R {
    checkIndex(index, range)
    return block()
}

/**
 * Performs the given [block] of code only if the given [index] is in range for
 * an [Int].
 *
 * @param index                         The index to check.
 * @param block                         The block to perform if the [index] is in range.
 * @return                              The result of running the [block].
 * @throws IndexOutOfBoundsException    If the [index] is not in range for an [Int].
 * @param R                             The return-type of the [block].
 */
inline fun <R> Int.Companion.withCheckedBitIndex(index : Int, block : () -> R) : R {
    Int.checkBitIndex(index)
    return block()
}

/**
 * Performs the given [block] of code only if the given [index] is in range for
 * an [Long].
 *
 * @param index                         The index to check.
 * @param block                         The block to perform if the [index] is in range.
 * @return                              The result of running the [block].
 * @throws IndexOutOfBoundsException    If the [index] is not in range for an [Long].
 * @param R                             The return-type of the [block].
 */
inline fun <R> Long.Companion.withCheckedBitIndex(index : Int, block : () -> R) : R {
    Long.checkBitIndex(index)
    return block()
}

/**
 * Returns an [Int] with a single one at the specified bit position.
 *
 * @param bit                           The index of the bit to return a selector for.
 * @return                              An [Int] with a single one at the specified
 *                                      bit-position.
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for an
 *                                      [Int].
 */
fun Int.Companion.bitSelector(bit : Int) : Int = Int.withCheckedBitIndex(bit) {
    return 1 shl bit
}

/**
 * Returns a [Long] with a single one at the specified bit position.
 *
 * @param bit                           The index of the bit to return a selector for.
 * @return                              A [Long] with a single one at the specified
 *                                      bit-position.
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for an
 *                                      [Long].
 */
fun Long.Companion.bitSelector(bit : Int) : Long = Long.withCheckedBitIndex(bit) {
    return 1L shl bit
}

/**
 * Gets the state of the bit at the specified position.
 *
 * @param bit                           The index of the bit to check.
 * @return                              True if the bit is a one, false if it is a zero.
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for an [Int].
 */
fun Int.bitState(bit : Int) : Boolean {
    return (Int.bitSelector(bit) and this) != 0
}

/**
 * Gets the state of the bit at the specified position.
 *
 * @param bit                           The index of the bit to check.
 * @return                              True if the bit is a one, false if it is a zero.
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for an [Long].
 */
fun Long.bitState(bit : Int) : Boolean {
    return (Long.bitSelector(bit) and this) != 0L
}

/**
 * Returns an [Int] identical to this one, but with a one at the specified
 * [bit]-position.
 *
 * @receiver                            An [Int].
 * @param bit                           The index of the bit to set.
 * @return                              An [Int] identical to this one, except with a
 *                                      one at the specified [bit]-position.
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for an [Int].
 */
fun Int.setBit(bit : Int) : Int {
    return Int.bitSelector(bit) or this
}

/**
 * Returns a [Long] identical to this one, but with a one at the specified
 * [bit]-position.
 *
 * @receiver                            A [Long].
 * @param bit                           The index of the bit to set.
 * @return                              A [Long] identical to this one, except with a
 *                                      one at the specified [bit]-position.
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for a [Long].
 */
fun Long.setBit(bit : Int) : Long {
    return Long.bitSelector(bit) or this
}

/**
 * Returns an [Int] identical to this one, but with a zero at the specified
 * [bit]-position.
 *
 * @receiver                            An [Int].
 * @param bit                           The index of the bit to clear.
 * @return                              An [Int] identical to this one, except with a
 *                                      zero at the specified [bit]-position.
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for an [Int].
 */
fun Int.clearBit(bit : Int) : Int {
    return Int.bitSelector(bit).inv() and this
}

/**
 * Returns a [Long] identical to this one, but with a zero at the specified
 * [bit]-position.
 *
 * @receiver                            A [Long].
 * @param bit                           The index of the bit to clear.
 * @return                              A [Long] identical to this one, except with a
 *                                      zero at the specified [bit]-position.
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for a [Long].
 */
fun Long.clearBit(bit : Int) : Long {
    return Long.bitSelector(bit).inv() and this
}

/**
 * Returns an [Int] identical to this one, but with the bit at the specified [bit]-position
 * set to the given [state].
 *
 * @receiver                            An [Int].
 * @param bit                           The index of the bit to modify.
 * @param state                         The state to set the [bit] to.
 * @return                              An [Int] identical to this one, except with
 *                                      the specified [bit]-position modified to the given [state].
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for an [Int].
 */
fun Int.modifyBit(bit : Int, state : Boolean) : Int {
    return if (state) setBit(bit) else clearBit(bit)
}

/**
 * Returns a [Long] identical to this one, but with the bit at the specified [bit]-position
 * set to the given [state].
 *
 * @receiver                            A [Long].
 * @param bit                           The index of the bit to modify.
 * @param state                         The state to set the [bit] to.
 * @return                              A [Long] identical to this one, except with
 *                                      the specified [bit]-position modified to the given [state].
 * @throws IndexOutOfBoundsException    If the specified [bit] is invalid for a [Long].
 */
fun Long.modifyBit(bit : Int, state : Boolean) : Long {
    return if (state) setBit(bit) else clearBit(bit)
}

/** The sign of the integer. */
val Int.sign : Int
    get() = if (this < 0) -1 else if (this > 0) 1 else 0

/** The sign of this long. */
val Long.sign : Int
    get() = if (this < 0L) -1 else if (this > 0L) 1 else 0
