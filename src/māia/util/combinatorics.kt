package māia.util

import java.math.BigInteger

/**
 * Calculates the number of possible permutations of a collection
 * of the given size.
 *
 * @param size
 *          The number of items in the collection.
 * @return
 *          The number of possible permutations of a collection
 *          of the given size.
 */
fun numPermutations(size: Int): BigInteger = numSubsets(size, size, true)

/**
 * Gets the number of ways to choose ``subsetSize`` items from a set of
 * ``setSize`` possibilities.
 *
 * @param setSize
 *          The number of items to select from.
 * @param subsetSize
 *          The number of items to select.
 * @param orderMatters
 *          Whether selections of the same items but in a different selection-order
 *          are considered distinct subsets.
 * @param canReselect
 *          Whether the same item can appear more than once in a subset.
 * @return
 *          The number of possible subsets that could be selected.
 */
fun numSubsets(
    setSize: Int,
    subsetSize: Int,
    orderMatters: Boolean = false,
    canReselect: Boolean = false
): BigInteger {
    // Sets can't have negative size
    require(setSize >= 0) { "Can't have a set of $setSize items" }
    require(subsetSize >= 0) { "Can't have a subset of $subsetSize items" }

    // Can only ever be one subset of size 0, the empty set
    if (subsetSize == 0) return BigInteger.ONE

    // If there are no items to select from, the empty set is the only possible selection,
    // so any subsets of greater size are impossible
    if (setSize == 0) return BigInteger.ZERO

    // Handle reselection separately
    val setSizeActual: Int = if (canReselect) {
        // If order matters, (n, k) = n^k
        if (orderMatters) return setSize.toBigInteger().pow(subsetSize)

        // Otherwise, (n, k) = (n + k - 1, k) (without reselection). Rather than recursing, we
        // just fall through with a modified n
        setSize + subsetSize - 1
    } else {
        // Without reselection, we can't select more items than are in the set
        if (subsetSize > setSize) return BigInteger.ZERO

        setSize
    }

    // If order matters, (n, k) = n! / (n - k)! (without reselection)
    if (orderMatters) return factorial(setSize, setSize - subsetSize)

    // Otherwise, (n, k) = n! / k!(n - k)! (again, without reselection).
    // We discriminate on the difference between n and k to determine
    // the least number of multiplications to perform
    val remainderSize = setSizeActual - subsetSize
    return if (subsetSize > remainderSize)
        factorial(setSizeActual, subsetSize) / factorial(remainderSize)
    else
        factorial(setSizeActual, remainderSize) / factorial(subsetSize)
}

/**
 * Returns the multiplication of all positive integers from ``of`` down
 * to (but not including) ``downTo``.
 *
 * @param of
 *          The greatest positive integer to include in the product.
 * @param downTo
 *          The greatest positive integer, less than ``of``, to exclude
 *          from the product.
 * @return
 *          The factorial of ``of`` down to ``downTo``. If ``of`` equals
 *          ``downTo``, the result is ``of``.
 */
fun factorial(
    of: Int,
    downTo: Int = 0
): BigInteger {
    require(downTo >= 0) { "'downTo' cannot be negative, got $downTo" }
    require(of >= downTo) { "'of' must be at least 'downTo', got 'of' = $of, 'downTo' = $downTo" }

    var result = BigInteger.ONE
    inlineRangeForLoop(of, downTo) {
        result *= of.toBigInteger()
    }

    return result
}

/**
 * A number representing a single subset of size ``subsetSize``,
 * selected from of a set of size ``setSize``.
 *
 * @param setSize
 *          The number of items in the original set.
 * @param subsetSize
 *          The number of items in the subset.
 * @param asBigInteger
 *          The value of the subset number.
 * @param orderMatters
 *          Whether selections of the same items but in a different selection-order
 *          are considered distinct subsets.
 * @param canReselect
 *          Whether the same item can appear more than once in the subset.
 */
data class SubsetNumber internal constructor(
    val setSize: Int,
    val subsetSize: Int,
    val asBigInteger: BigInteger,
    val orderMatters : Boolean = false,
    val canReselect : Boolean = false
) {
    companion object {
        /**
         *  Creates an arbitrary subset number. Checks that it is valid for the
         *  given parameters.
         *
         * @param value
         *          The value of the subset number.
         * @param setSize
         *          The number of items in the original set.
         * @param subsetSize
         *          The number of items in the subset.
         * @param orderMatters
         *          Whether selections of the same items but in a different selection-order
         *          are considered distinct subsets.
         * @param canReselect
         *          Whether the same item can appear more than once in the subset.
         */
        fun create(
            value: BigInteger,
            setSize: Int,
            subsetSize: Int,
            orderMatters : Boolean = false,
            canReselect : Boolean = false
        ): SubsetNumber {
            require(value >= BigInteger.ZERO) {
                "Subset number must be greater than zero, got $value"
            }

            val numSubsets = numSubsets(setSize, subsetSize, orderMatters, canReselect)

            require(value < numSubsets) {
                "Subset number must be less than $numSubsets, got $value"
            }

            return SubsetNumber(setSize, subsetSize, value, orderMatters, canReselect)
        }

        /**
         * Encodes a subset as a subset number.
         *
         * @param subset
         *          The selected items by index.
         * @param setSize
         *          The number of items to select from.
         * @param orderMatters
         *          Whether the selection order of the subset should be encoded as well.
         * @param canReselect
         *          Whether duplicate items are allowed in the subset.
         * @return
         *          The subset number of the subset.
         */
        fun fromSubset(
            subset: IntArray,
            setSize: Int,
            orderMatters : Boolean = false,
            canReselect : Boolean = false
        ): SubsetNumber {
            // Cache the size of the subset
            val subsetSize = subset.size

            // Sets can't have negative size
            require(setSize >= 0) { "setSize must be non-negative, got $setSize" }

            // The empty set is the only possible subset of size 0, so encode it as the smallest representation
            if (subsetSize == 0) return SubsetNumber(setSize, subsetSize, BigInteger.ZERO, orderMatters, canReselect)

            // If there are no items to select from, the empty set is the only possible selection,
            // so any subsets of greater size are impossible
            require(setSize > 0) { "Can't select a non-empty subset (subset size = $subsetSize) from the empty set" }

            // Make sure all values are in [0, n)
            require(subset.iterator().all { 0 <= it && it < setSize }) {
                "Subset contains values outside the valid range for a set-size of $setSize: $subset"
            }

            // If not allowed to reselect, subset cannot contain duplicates
            if (!canReselect) {
                val firstDuplicate = subset.iterator().asIterable().findFirstDuplicate()
                require(firstDuplicate is Absent) {
                    "Duplicate items in subset: $subset"
                }
            }

            // Select the number-generating function
            val subsetNumberFunc = if (orderMatters && canReselect)
                ::subsetNumberFromSubsetOrderMattersCanReselect
            else if (orderMatters)
                ::subsetNumberFromSubsetOrderMatters
            else if (canReselect)
                ::subsetNumberFromSubsetCanReselect
            else
                ::subsetNumberFromSubset

            return SubsetNumber(
                setSize,
                subsetSize,
                subsetNumberFunc(subset, setSize),
                orderMatters,
                canReselect
            )
        }

        /**
         * Encodes a subset as a subset number. Assumes order matters and items
         * can be reselected. Assumes arguments have already been checked for
         * validity.
         *
         * @param subset
         *          The selected items by index.
         * @param setSize
         *          The number of items selected from.
         * @return
         *          The subset number of the subset.
         */
        private fun subsetNumberFromSubsetOrderMattersCanReselect(
            subset: IntArray,
            setSize: Int
        ): BigInteger {
            // Shift-encode each value in order
            return subset
                    .iterator()
                    .enumerate()
                    .map { (index, value) -> setSize.toBigInteger().pow(index) * value.toBigInteger() }
                    .asIterable()
                    .sumOf { it }
        }

        /**
         * Encodes a subset as a subset number. Assumes order matters and items
         * can't be reselected. Assumes arguments have already been checked for
         * validity.
         *
         * @param subset
         *          The selected items by index.
         * @param setSize
         *          The number of items selected from.
         * @return
         *          The subset number of the subset.
         */
        private fun subsetNumberFromSubsetOrderMatters(
            subset: IntArray,
            setSize: Int
        ): BigInteger {
            // Shift-encode the items, reducing the problem to (n - 1, k -1)
            // at each iteration
            var result = subset[0].toBigInteger()
            var remainingSubset = IntArray(subset.size - 1) {
                val value = subset[it + 1]
                if (value.toBigInteger() > result) value - 1 else value
            }
            var factor = setSize - 1
            while (remainingSubset.isNotEmpty()) {
                result *= factor.toBigInteger()
                val next = remainingSubset[0]
                result += next.toBigInteger()
                factor--
                remainingSubset = IntArray(remainingSubset.size - 1) {
                    val value = remainingSubset[it + 1]
                    if (value > next) value - 1 else value
                }
            }

            return result
        }

        /**
         * Encodes a subset as a subset number. Assumes order doesn't matter and items
         * can be reselected. Assumes arguments have already been checked for
         * validity.
         *
         * @param subset
         *          The selected items by index.
         * @param setSize
         *          The number of items selected from.
         * @return
         *          The subset number of the subset.
         */
        private fun subsetNumberFromSubsetCanReselect(
            subset: IntArray,
            setSize: Int
        ): BigInteger {
            // Convert to the equivalent binomial representation and fall-through encode
            val counts = HashMap<Int, Int>()
            for (value in subset) counts[value] = counts[value]?.inc() ?: 1
            var last = -1
            val modifiedSubset = IntArray(setSize - 1) {
                last += 1 + (counts[it] ?: 0)
                last
            }

            return subsetNumberFromSubset(
                modifiedSubset,
                setSize + subset.size - 1
            )
        }

        /**
         * Encodes a subset as a subset number. Assumes order doesn't matter and items
         * can't be reselected. Assumes arguments have already been checked for
         * validity.
         *
         * @param subset
         *          The selected items by index.
         * @param setSize
         *          The number of items selected from.
         * @return
         *          The subset number of the subset.
         */
        private fun subsetNumberFromSubset(
            subset: IntArray,
            setSize: Int
        ): BigInteger {
            // Encode the sorted subset as an arithmetic encoding
            val sorted = subset.copyOf()
            sorted.sortDescending()

            return sorted
                .iterator()
                .enumerate()
                .map { (k, n) -> numSubsets(n, subset.size - k) }
                .asIterable()
                .sumOf { it }
        }
    }

    /**
     * Decodes a subset number into the original subset.
     *
     * @return
     *          The originally encoded subset.
     */
    fun toSubset(): IntArray {
        // Special case for order-dependent
        if (orderMatters) {
            // Ordered with reselection is shift-encoded, so simply shift-decode
            if (canReselect) {
                var remainingSubsetNumber = asBigInteger
                val bigSetSize = setSize.toBigInteger()
                return IntArray(subsetSize) {
                    val value = (remainingSubsetNumber % bigSetSize).intValueExact()
                    remainingSubsetNumber /= bigSetSize
                    value
                }
            } else {
                // Without reselection, the items available for selection reduces by 1 at each iteration
                var factor = (setSize - subsetSize + 1).toBigInteger()
                var remainingSubsetNumber = asBigInteger
                val subset = IntArray(subsetSize)
                inlineRangeForLoop(subsetSize) {
                    val next = (remainingSubsetNumber % factor).intValueExact()
                    remainingSubsetNumber /= factor
                    inlineRangeForLoop(subsetSize - it, subsetSize) {
                        if (subset[it] >= next) subset[it]++
                    }
                    subset[subsetSize - it - 1] = next
                    factor++
                }
                return subset
            }
        }

        // If reselect is allowed, we are expecting the equivalent binomial representation of the selection
        val setSizeActual = if (canReselect) setSize + subsetSize - 1 else setSize
        val subsetSizeActual = if (canReselect) setSize - 1 else subsetSize

        // Decode the arithmetic encoding of the binomial representation
        var numSubsets = numSubsets(setSizeActual - 1, subsetSize)
        var k = subsetSizeActual.toBigInteger()
        var i = 0
        var remainingSubsetNumber = asBigInteger
        val subset = IntArray(subsetSizeActual)
        inlineRangeForLoop(start=setSizeActual - 1, end=-1) {
            if (remainingSubsetNumber >= numSubsets) {
                remainingSubsetNumber -= numSubsets
                subset[i] = it
                i++
                if (i == subsetSizeActual) throw LoopControl.Break
                numSubsets = (numSubsets * k) / it.toBigInteger()
                k--
            } else if (it != 0) {
                val n = it.toBigInteger()
                numSubsets = (numSubsets * (n - k)) / n
            }
        }

        // Convert the binomial representation back to the original multinomial one if reselection was enabled
        if (canReselect) {
            subset.sort()
            val counts = HashMap<Int, Int>()
            var total = 0
            inlineRangeForLoop(setSize - 1) {
                val last = if (it == 0) -1 else subset[it-1]
                val count = subset[it] - last - 1
                total += count
                if (count != 0)
                    counts[it] = count
            }
            if (total < subsetSize) {
                counts[setSize - 1] = subsetSize - total
            }
            val subsetActual = IntArray(subsetSize)
            var index = 0
            for ((value, count) in counts.entries) {
                inlineRangeForLoop(count) {
                    subsetActual[index] = value
                    index++
                }
            }
            return subsetActual
        }

        return subset
    }
}
