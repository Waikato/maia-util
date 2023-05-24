package maia.util

/**
 * Package of utility functions related to big integers.
 */

import java.math.BigInteger
import kotlin.math.min
import kotlin.random.Random

/**
 * Generates a random [BigInteger] in the range [[from], [to]).
 *
 * @receiver
 *          A source of randomness.
 * @param from
 *          The smallest possible value to return.
 * @param to
 *          The exclusive largest value to return.
 * @return
 *          A [BigInteger] in [[from], [to]).
 * @throws IllegalArgumentException
 *          If [to] <= [from].
 */
fun Random.nextBigInteger(from : BigInteger, to : BigInteger) : BigInteger {
    // to must be greater than from
    if (to <= from) throw IllegalArgumentException("to ($to) must be greater than from ($from)")

    return nextBigInteger(to - from) + from
}

/**
 * Generates a random [BigInteger] in the range [0, [to]).
 *
 * @receiver
 *          A source of randomness.
 * @param to
 *          The exclusive largest value to return.
 * @return
 *          A [BigInteger] in [0, [to]).
 * @throws IllegalArgumentException
 *          If [to] <= 0.
 */
fun Random.nextBigInteger(to : BigInteger) : BigInteger {
    // Make sure the range is strictly positive
    if (to <= BigInteger.ZERO) throw IllegalArgumentException("to ($to) must be greater than 0")

    // If the exclusive maximum is a power of two, this is the same as generating
    // random bits up to the bit-size of the inclusive maximum
    if (to.isPowerOfTwo) return nextBigBits(to.lowestSetBit)

    // If the exclusive maximum is not a power-of-two, round it up
    // to the nearest power-of-two, and just generate random numbers
    // until one of them is in-range.
    // The probability that a given number will be suitable depends
    // on the source of randomness, but if it has a uniform distribution
    // then the probability is greater than 0.5, and so the probability that
    // this loop runs for N iterations is less than 2^-N.
    val upper = BigInteger.ZERO.setBit(to.bitLength())
    while (true) {
        val next = nextBigInteger(upper)
        if (next < to) return next
    }
}

/**
 * Generates a [BigInteger] with the specified number of
 * random bits.
 *
 * @receiver
 *          A source of randomness.
 * @param bitCount
 *          The number of bits to generate.
 * @return
 *          A non-negative [BigInteger].
 * @throws IllegalArgumentException
 *          If [bitCount] is negative.
 */
fun Random.nextBigBits(bitCount : Int) : BigInteger {
    // Make sure the bit-count is non-negative
    if (bitCount < 0) throw IllegalArgumentException("bitCount ($bitCount) must be non-negative")

    // Keep track of how many more bits we need to generate
    var bitsRemaining = bitCount

    // Create a store of the bits we've generated so far
    var value = BigInteger.ZERO

    // Keep generating bits until we've got enough
    while (bitsRemaining > 0) {
        // nextBits generates Ints, and we only want positive Ints, so generate
        // at most 31 bits at a time
        val nextBitCount = min(bitsRemaining, 31)

        // Shift the current bits left to make way for the new ones (and zeroes :P)
        value = value.shiftLeft(nextBitCount)

        // Generate an Int's-worth of bits
        val bits = nextBits(nextBitCount).toBigInteger()

        // OR the new bits into our store
        value = value or bits

        // Update the number left to generate
        bitsRemaining -= nextBitCount
    }

    return value
}

/**
 * Extension operator which allows addition of [Int]s to
 * [BigInteger]s.
 *
 * @receiver
 *          The left-operand of the addition.
 * @param value
 *          The right-operand of the addition.
 * @return
 *          The [BigInteger] sum of the operands.
 */
operator fun BigInteger.plus(value : Int) : BigInteger {
    return this + value.toBigInteger()
}

/**
 * Extension operator which allows subtraction of [Int]s from
 * [BigInteger]s.
 *
 * @receiver
 *          The left-operand of the subtraction.
 * @param value
 *          The right-operand of the subtraction.
 * @return
 *          The [BigInteger] difference between the operands.
 */
operator fun BigInteger.minus(value : Int) : BigInteger {
    return this - value.toBigInteger()
}

/**
 * Extension operator which allows multiplication of [Int]s with
 * [BigInteger]s.
 *
 * @receiver
 *          The left-operand of the subtraction.
 * @param value
 *          The right-operand of the subtraction.
 * @return
 *          The [BigInteger] product of the operands.
 */
operator fun BigInteger.times(value : Int) : BigInteger {
    return this * value.toBigInteger()
}

// TODO: Other arithmetic operations on Ints
// TODO: Arithmetic operations on Longs

/**
 * Extension property indicating if a [BigInteger] is a power-of-two.
 */
val BigInteger.isPowerOfTwo
    get() = this > BigInteger.ZERO && lowestSetBit == bitLength() - 1
