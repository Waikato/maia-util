package māia.util

/*
 * Statistical functions used by NaiveBayes.
 *
 * TODO: Should this be a core package?
 */

import kotlin.math.*

const val MAXLOG = 7.09782712893383996732E2
const val SQRTH = 7.07106781186547524401E-1

fun normalProbability(a : Double) : Double {

    val x = a * SQRTH
    val z = abs(x)

    var y : Double
    if (z < SQRTH) {
        y = 0.5 + 0.5 * errorFunction(x)
    } else {
        y = 0.5 * errorFunctionComplemented(z)
        if (x > 0) y = 1.0 - y
    }
    return y
}

fun errorFunction(x : Double) : Double {
    if (abs(x) > 1.0) return 1.0 - errorFunctionComplemented(x)

    val T = doubleArrayOf(9.60497373987051638749E0, 9.00260197203842689217E1,
            2.23200534594684319226E3, 7.00332514112805075473E3,
            5.55923013010394962768E4)

    val U = doubleArrayOf(3.35617141647503099647E1, 5.21357949780152679795E2,
            4.59432382970980127987E3, 2.26290000613890934246E4,
            4.92673942608635921086E4)

    val z = x * x
    return x * polevl(z, T, 4) / p1evl(z, U, 5)
}

fun errorFunctionComplemented(a: Double): Double {
    var y: Double
    val p: Double
    val q: Double
    val P = doubleArrayOf(2.46196981473530512524E-10, 5.64189564831068821977E-1,
            7.46321056442269912687E0, 4.86371970985681366614E1,
            1.96520832956077098242E2, 5.26445194995477358631E2,
            9.34528527171957607540E2, 1.02755188689515710272E3,
            5.57535335369399327526E2)
    val Q = doubleArrayOf( // 1.0
            1.32281951154744992508E1, 8.67072140885989742329E1,
            3.54937778887819891062E2, 9.75708501743205489753E2,
            1.82390916687909736289E3, 2.24633760818710981792E3,
            1.65666309194161350182E3, 5.57535340817727675546E2)
    val R = doubleArrayOf(5.64189583547755073984E-1, 1.27536670759978104416E0,
            5.01905042251180477414E0, 6.16021097993053585195E0,
            7.40974269950448939160E0, 2.97886665372100240670E0)
    val S = doubleArrayOf( // 1.00000000000000000000E0,
            2.26052863220117276590E0, 9.39603524938001434673E0,
            1.20489539808096656605E1, 1.70814450747565897222E1,
            9.60896809063285878198E0, 3.36907645100081516050E0)
    val x: Double = if (a < 0.0) {
        -a
    } else {
        a
    }
    if (x < 1.0) {
        return 1.0 - errorFunction(a)
    }
    var z: Double = -a * a
    if (z < -MAXLOG) {
        return if (a < 0) {
            2.0
        } else {
            0.0
        }
    }
    z = exp(z)
    if (x < 8.0) {
        p = polevl(x, P, 8)
        q = p1evl(x, Q, 8)
    } else {
        p = polevl(x, R, 5)
        q = p1evl(x, S, 6)
    }
    y = z * p / q
    if (a < 0) {
        y = 2.0 - y
    }
    return if (y == 0.0) {
        if (a < 0) {
            2.0
        } else {
            0.0
        }
    } else y
}

fun polevl(x: Double, coef: DoubleArray, N: Int): Double {
    var ans: Double
    ans = coef[0]
    for (i in 1..N) {
        ans = ans * x + coef[i]
    }
    return ans
}

fun p1evl(x: Double, coef: DoubleArray, N: Int): Double {
    var ans: Double
    ans = x + coef[0]
    for (i in 1 until N) {
        ans = ans * x + coef[i]
    }
    return ans
}

/**
 * An estimation of the weight less than, equal to and greater than a value.
 */
inline class WeightsEstimation private constructor(
    private val asArray : DoubleArray
) {
    constructor(
        lessThan: Double,
        equalTo: Double,
        greaterThan: Double
    ): this(doubleArrayOf(lessThan, equalTo, greaterThan))

    val lessThanValue: Double
        get() = asArray[0]

    val equalToValue: Double
        get() = asArray[1]

    val greaterThenValue: Double
        get() = asArray[2]
}

class GaussianEstimator {

    companion object {
        val NORMAL_CONSTANT = sqrt(2 * Math.PI)
    }

    var minObserved: Double = Double.POSITIVE_INFINITY
        private set

    var maxObserved: Double = Double.NEGATIVE_INFINITY
        private set

    var weightSum = 0.0
        private set

    var mean = 0.0
        private set

    var varianceSum = 0.0
        private set

    val variance: Double
        get() = if (weightSum > 1.0)
            varianceSum / (weightSum - 1.0)
        else
            0.0

    val stdDev: Double
        get() = sqrt(variance)


    fun observe(
        value: Double,
        weight: Double
    ) {
        if (value.isInfinite() || value.isNaN()) return

        minObserved = min(minObserved, value)
        maxObserved = max(maxObserved, value)

        if (weightSum == 0.0) {
            mean = value
            weightSum = weight
            return
        }

        weightSum += weight
        val lastMean = mean
        val variance = weight * (value - lastMean)
        mean += variance / weightSum
        varianceSum += variance * (value - mean)
    }

    fun probabilityDensity(value: Double): Double {
        if (weightSum == 0.0) return 0.0

        val stdDev = stdDev

        if (stdDev == 0.0) return if (value == mean) 1.0 else 0.0

        val diff = value - mean

        val result = (1.0 / (NORMAL_CONSTANT * stdDev)) * exp(-(diff * diff / (2.0 * stdDev * stdDev)))

        return result
    }

    fun estimatedWeightsComparedToValue(value: Double): WeightsEstimation {
        val equalToWeight = probabilityDensity(value) * weightSum

        val stdDev = stdDev

        val lessThanWeight = if (stdDev > 0.0)
            normalProbability((value - mean) / stdDev) * weightSum - equalToWeight
        else if (value < mean)
            weightSum - equalToWeight
        else
            0.0

        val greaterThanWeight = (weightSum - equalToWeight - lessThanWeight)
            .coerceAtLeast(0.0)

        return WeightsEstimation(lessThanWeight, equalToWeight, greaterThanWeight)
    }
}
