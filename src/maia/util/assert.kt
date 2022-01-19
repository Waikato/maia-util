package maia.util

/**
 * Asserts that the given value is of the given type.
 *
 * @param value The value.
 * @param T The asserted type of the [value].
 *
 * @return [value] cast to a [T].
 */
inline fun <reified T> assertType(
    value: Any?
): T {
    return value as T
}
