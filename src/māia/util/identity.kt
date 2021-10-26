package māia.util

/**
 * The identity function. Returns its argument.
 *
 * @param value
 *          The value to return.
 * @return [value].
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
inline fun <T> identity(value: T): T {
    return value
}
