package maia.util.error

/**
 * Error thrown when code that is asserted to be unreachable,
 * is, in fact, reached.
 *
 * @param reason The explanation of why this line should be unreachable.
 */
class UnreachableCode(
    reason: String = "Code that was assumed unreachable was reached. This should not have happened."
): Error(
    reason
)

/**
 * Asserts that the line of code that this statement is on is unreachable.
 *
 * @throws UnreachableCode Always.
 */
inline fun UNREACHABLE_CODE(): Nothing = throw UnreachableCode()

/**
 * Asserts that the line of code that this statement is on is unreachable.
 *
 * @param reason The explanation of why this line should be unreachable.
 *
 * @throws UnreachableCode Always.
 */
inline fun UNREACHABLE_CODE(reason: String): Nothing = throw UnreachableCode(reason)
