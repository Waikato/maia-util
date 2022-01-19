package maia.util

/**
 * Package for working with if-then-else-like structures.
 */

/**
 * Helper class which provides the ability to specify
 * an action to take when an if-statement succeeds via
 * the [then] method.
 *
 * @param success
 *          Whether the if-statement succeeded.
 */
class ThenContinuation private constructor(
        val success : Boolean
) {
    /**
     * Allows the action to take when the if-statement succeeds to
     * be specified.
     *
     * @param block
     *          The action to take if the if-statement succeeds.
     * @return
     *          An object allowing for specification of an else-like
     *          block.
     * @param R
     *          The return-type of the then-block.
     */
    inline infix fun <R> then(block : () -> R) : OtherwiseContinuation<R> {
        return if (success)
            OtherwiseContinuation(block())
        else
            OtherwiseContinuation()
    }

    operator fun not() : ThenContinuation {
        return ThenContinuation(!success)
    }
}

/**
 * Helper class which provides the ability to specify
 * an action to take when an if-statement succeeds via
 * the [then] method. Assumes that the if-statement returns
 * some sort of value on success, but not on failure.
 *
 * @param value
 *          The value resulting from the if-statement, if it succeeded.
 * @param success
 *          Whether the if-statement succeeded.
 * @param T
 *          The type of value resulting from a successful if-statement.
 */
class ThenContinuationWithSuccessValue<T> private constructor(
        val value : T?,
        val success : Boolean
) {
    /**
     * Constructor for when the if-statement succeeds.
     *
     * @param value
     *          The value resulting from the if-statement.
     */
    constructor(value : T) : this(value, true)

    /**
     * Constructor for when the if-statement fails.
     */
    constructor() : this(null , false)

    /**
     * Allows the action to take when the if-statement succeeds to
     * be specified.
     *
     * @param block
     *          The action to take if the if-statement succeeds.
     * @return
     *          An object allowing for specification of an else-like block.
     * @param R
     *          The return-type of the then-block.
     */
    inline infix fun <R> then(block : (T) -> R) : OtherwiseContinuation<R> {
        return if (success)
            @Suppress("UNCHECKED_CAST")
            OtherwiseContinuation(block(value as T))
        else
            OtherwiseContinuation()
    }

    operator fun not() : ThenContinuationWithFailureValue<T> {
        return if (success)
            @Suppress("UNCHECKED_CAST")
            ThenContinuationWithFailureValue(value as T)
        else
            ThenContinuationWithFailureValue()
    }
}

/**
 * Helper class which provides the ability to specify
 * an action to take when an if-statement succeeds via
 * the [then] method. Assumes that the if-statement returns
 * some sort of value on failure, but not on success.
 *
 * @param value
 *          The value resulting from the if-statement, if it failed.
 * @param success
 *          Whether the if-statement succeeded.
 * @param T
 *          The type of value resulting from a failing if-statement.
 */
class ThenContinuationWithFailureValue<T> private constructor(
        val value : T?,
        val success : Boolean
) {
    /**
     * Constructor for when the if-statement succeeds.
     */
    constructor() : this(null , true)

    /**
     * Constructor for when the if-statement fails.
     *
     * @param value
     *          The value resulting from the if-statement.
     */
    constructor(value : T) : this(value, false)

    /**
     * Allows the action to take when the if-statement succeeds to
     * be specified.
     *
     * @param block
     *          The action to take if the if-statement succeeds.
     * @return
     *          An object allowing for specification of an else-like block.
     * @param R
     *          The return-type of the then-block.
     */
    inline infix fun <R> then(block : () -> R) : OtherwiseContinuationWithFailureValue<T, R> {
        return if (success)
            OtherwiseContinuationWithFailureValue.resolved(block())
        else
            @Suppress("UNCHECKED_CAST")
            OtherwiseContinuationWithFailureValue.unresolved(value as T)
    }

    operator fun not() : ThenContinuationWithSuccessValue<T> {
        return if (success)
            ThenContinuationWithSuccessValue()
        else
            @Suppress("UNCHECKED_CAST")
            ThenContinuationWithSuccessValue(value as T)
    }
}

/**
 * Helper class which provides the ability to specify
 * an action to take when an if-statement succeeds via
 * the [then] method. Assumes that the if-statement returns
 * some sort of value on failure or success.
 *
 * @param value
 *          The value resulting from the if-statement.
 * @param success
 *          Whether the if-statement succeeded.
 * @param T
 *          The type of value resulting from the if-statement.
 */
class ThenContinuationWithValue<T> private constructor(
        val value : T,
        val success : Boolean
) {
    /**
     * Allows the action to take when the if-statement succeeds to
     * be specified.
     *
     * @param block
     *          The action to take if the if-statement succeeds.
     * @return
     *          An object allowing for specification of an else-like block.
     * @param R
     *          The return-type of the then-block.
     */
    inline infix fun <R> then(block : (T) -> R) : OtherwiseContinuationWithFailureValue<T, R> {
        return if (success)
            OtherwiseContinuationWithFailureValue.resolved(block(value))
        else
            OtherwiseContinuationWithFailureValue.unresolved(value)
    }
}

/**
 * Helper class which provides the ability to specify
 * an action to take when an if-statement fails via
 * the [otherwise] method.
 *
 * @param result
 *          The value resulting from the then-block, or null if the
 *          if-statement failed.
 * @param resolved
 *          Whether the if-statement succeeded.
 * @param R
 *          The return type of the then/else-blocks.
 */
class OtherwiseContinuation<R> private constructor(
        val result : R?,
        val resolved : Boolean
) {
    /**
     * Constructor for when the if-statement succeeds.
     *
     * @param result
     *          The result of the then-block.
     */
    constructor(result : R) : this(result, true)

    /**
     * Constructor for when the if-statement fails.
     */
    constructor() : this(null, false)

    /**
     * Allows the action to take when the if-statement fails to
     * be specified.
     *
     * @param block
     *          The action to take if the if-statement fails.
     * @return
     *          The result of the then-block if the if-statement succeeded,
     *          or the result of the else-block if it failed.
     */
    inline infix fun otherwise(block : () -> R) : R {
        return if (resolved)
            @Suppress("UNCHECKED_CAST")
            result as R
        else
            block()
    }
}

/**
 * Helper class which provides the ability to specify
 * an action to take when an if-statement fails via
 * the [otherwise] method. Assumes that the if-statement
 * produces a value when it fails.
 *
 * @param value
 *          The value resulting from the if-statement if it failed, or null
 *          if it succeeded.
 * @param result
 *          The value resulting from the then-block, or null if the
 *          if-statement failed.
 * @param resolved
 *          Whether the if-statement succeeded.
 * @param T
 *          The type of value produced on failure of the if-statement.
 * @param R
 *          The return type of the then/else-blocks.
 */
class OtherwiseContinuationWithFailureValue<T, R> private constructor(
        val value : T?,
        val result : R?,
        val resolved : Boolean
) {

    /**
     * Allows the action to take when the if-statement fails to
     * be specified.
     *
     * @param block
     *          The action to take if the if-statement fails.
     * @return
     *          The result of the then-block if the if-statement succeeded,
     *          or the result of the else-block if it failed.
     */
    inline infix fun otherwise(block : (T) -> R) : R {
        return if (resolved)
            @Suppress("UNCHECKED_CAST")
            result as R
        else
            @Suppress("UNCHECKED_CAST")
            block(value as T)
    }

    companion object {

        /**
         * Constructor for when the if-statement succeeds.
         *
         * @param result
         *          The result of the then-block.
         *
         * @see OtherwiseContinuationWithFailureValue
         */
        fun <T, R> resolved(result : R) : OtherwiseContinuationWithFailureValue<T, R> {
            return OtherwiseContinuationWithFailureValue(null, result, true)
        }

        /**
         * Constructor for when the if-statement fails.
         *
         * @param value
         *          The value produced by the failing if-statement.
         *
         * @see OtherwiseContinuationWithFailureValue
         */
        fun <T, R> unresolved(value : T) : OtherwiseContinuationWithFailureValue<T, R> {
            return OtherwiseContinuationWithFailureValue(value, null, false)
        }
    }
}
