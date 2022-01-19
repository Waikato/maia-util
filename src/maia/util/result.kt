package maia.util

/**
 * Represents the (possibly-erroneous) result of some operation.
 *
 * @param T
 *          The type of the successful result.
 * @param E
 *          The type of the erroneous result.
 */
sealed class Result<out T, out E>

/**
 * Represents the succesful result of an operation.
 *
 * @param T
 *          The type of the successful result.
 */
class Success<out T>(
    val value: T
) : Result<T, Nothing>()

/**
 * Represents the erroneous result of an operation.
 *
 * @param E
 *          The type of the erroneous result.
 */
class Failure<out E>(
    val error: E
) : Result<Nothing, E>()
