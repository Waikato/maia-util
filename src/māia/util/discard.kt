package māia.util

/**
 * Utility function which executes a function and discards the result.
 * Useful for when delegating a function which is expected to return
 * [Unit] to a function which does not.
 *
 * @param block
 *          The function to perform.
 */
inline fun <T> discard(block : () -> T) {
    block()
}
