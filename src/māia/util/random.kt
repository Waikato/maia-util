package māia.util

import māia.util.property.ReadOnlyPropertyWithMemory
import kotlin.random.Random
import kotlin.reflect.KProperty

/**
 * Gets an array of random doubles of the selected size.
 *
 * @receiver
 *          The [Random] instance to use to generate values.
 * @param size
 *          The size of the double array to create.
 * @return
 *          The randomised double array.
 */
fun Random.nextDoubleArray(size: Int) : DoubleArray = DoubleArray(size) { nextDouble() }

/**
 * Returns a random index of the given array of weights, weighted by the value
 * at each index.
 *
 * @receiver
 *          The [Random] instance to use to generate values.
 * @param weights
 *          The weight for each index.
 * @return
 *          A randomly-selected index.
 */
fun Random.nextIntWeighted(weights: DoubleArray) : Int {
    // Calculate the total weight
    val totalWeight = weights.sum()

    // Pick a random value in the range of the total weight
    val pick = nextDouble(totalWeight)

    // Pick the index with accumulated weight greater than the chosen value
    var sum = 0.0
    for (index in weights.indices) {
        sum += weights[index]
        if (pick < sum) return index
    }

    // Should only occur in the case of rounding error
    return weights.lastIndex
}

/**
 * Imbues a [kotlin.random.Random] with the [java.util.Random.nextGaussian]
 * method.
 */
val Random.nextGaussian by object
    : ReadOnlyPropertyWithMemory<Random, () -> Double, Double>(true) {
    override fun getValue(
        thisRef : Random,
        property : KProperty<*>
    ) : () -> Double {
        return fun(): Double {
            if (thisRef in memory)
                return memory[thisRef].also { memory -= thisRef }

            var v1 : Double
            var v2 : Double
            var s : Double

            do {
                v1 = thisRef.nextDouble(-1.0, 1.0)
                v2 = thisRef.nextDouble(-1.0, 1.0)
                s = v1 * v1 + v2 * v2
            } while (s >= 1 || s == 0.0)

            val multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s)

            memory[thisRef] = v2 * multiplier

            return v1 * multiplier
        }
    }
}
