package maia.util

/**
 * Represents a probability between 0.0 and 1.0.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
@JvmInline
value class Probability(val asDouble: Double) {

    init {
        // Ensure the double is in the valid range for probabilities
        if (asDouble !in VALID_PROBABILITY_RANGE)
            throw IllegalArgumentException(
                "Probabilities can only be in [$VALID_PROBABILITY_RANGE], got $asDouble"
            )
    }

    companion object {
        /** No chance of the event occurring. */
        val NONE: Probability = Probability(0.0)

        /** The event will definitely occur. */
        val DEFINITE: Probability = Probability(1.0)

        /** The range of valid values a probability can take. */
        val VALID_PROBABILITY_RANGE = 0.0 .. 1.0
    }

}
