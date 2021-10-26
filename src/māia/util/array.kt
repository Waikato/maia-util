package māia.util

import kotlin.math.sqrt

/**
 * Modifies each value in the double array according to the given [block].
 *
 * @param block
 *          The modification to make.
 */
inline fun DoubleArray.mapInPlaceIndexed(crossinline block: (Int, Double) -> Double) {
    forEachIndexed { index, value ->
        this[index] = block(index, value)
    }
}

/**
 * Modifies each value in the double array according to the given [block].
 *
 * @param block
 *          The modification to make.
 */
inline fun DoubleArray.mapInPlace(crossinline block: (Double) -> Double) {
    forEachIndexed { index, value ->
        this[index] = block(value)
    }
}

/**
 * Modifies each value in the double array according to the given [block].
 *
 * @param block
 *          The modification to make.
 */
inline fun DoubleArray.mapInPlaceIndexed(crossinline block: (Int) -> Double) {
    var index = 0
    val size = size
    while (index < size) {
        this[index] = block(index)
        index++
    }
}

/**
 * Gets the index of the element in the array with the greatest value.
 */
val DoubleArray.maxIndex: Int
    get() {
        var maxIndex = 0
        reduceIndexed { index, current, next ->
            if (next > current) {
                maxIndex = index
                next
            } else {
                current
            }
        }
        return maxIndex
    }

/**
 * Gets the magnitude of the array treated as a vector.
 *
 * @return
 *          The square-root of the sum-of-squares of the values in the array.
 */
val DoubleArray.magnitude: Double
    get() = sqrt(sumOf { it * it })

/**
 * Counts the number of non-zero entries in the array.
 */
val DoubleArray.nonZeroCount: Int
    get() = iterator().filter { it != 0.0 }.asIterable().count()

/**
 * Class which concatenates double arrays into a single array.
 */
class DoubleArrayBuilder() {

    private val arrays = ArrayList<DoubleArray>();

    operator fun plusAssign(value: DoubleArray) {
        arrays.add(value)
    }

    fun build(): DoubleArray {
        val totalSize = arrays.iterator().map {
            it.size
        }.asIterable().reduce(INT_SUM_REDUCER::reduce)

        val result = DoubleArray(totalSize)

        var index = 0
        for (array in arrays) {
            array.copyInto(result, index)
            index += array.size
        }

        return result
    }

}

@JvmInline
value class DoubleArrayReadOnlyView(
    private val source: DoubleArray
) {
    /**
     * Returns the array element at the given [index].  This method can be called using the index operator.
     *
     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException] except in Kotlin/JS
     * where the behavior is unspecified.
     */
    public operator fun get(index: Int): Double = source[index]

    /** Returns the number of elements in the array. */
    public val size: Int
        get() = source.size

    /** Creates an iterator over the elements of the array. */
    public operator fun iterator(): DoubleIterator = source.iterator()
}

/**
 * Multiplies all values in the array by the given [factor].
 *
 * @receiver The [DoubleArray].
 * @param factor The factor to multiply all values by.
 */
operator fun DoubleArray.timesAssign(factor: Double) {
    mapInPlace { it * factor }
}
