package maia.util

/**
 * Package for helper methods for using non-inline functions like inline
 * functions, without inlining them.
 *
 * When a public method of a class is declared inline, it can no longer use
 * non-public methods of the class. These functions imitate features of inline
 * functions without needing to declare the functions inline, so that non-public
 * methods can be called while retaining the benefits of inlining.
 */

/**
 * Exception which enables the 'inline' feature of non-local returns.
 *
 * @param value
 *          The return value.
 */
private class NonLocalReturnException(val value : Any?) : Exception()

/**
 * Helper class which can be used as a receiver to a block and adds a non-local
 * return function to the scope of the block.
 *
 * @param R
 *          The return-type of the block.
 */
class NoInline<R> {

    /**
     * Performs a non-local return of the given value.
     *
     * @param value
     *          The return value.
     * @throws NonLocalReturnException
     *          With the provided value.
     */
    fun nonLocalReturn(value : R) : Nothing {
        throw NonLocalReturnException(value)
    }

    /**
     * Casts a value of any type to the return-type.
     *
     * @param value
     *          The value to cast.
     * @return
     *          The value cast to the return-type.
     */
    internal fun cast(value : Any?) : R {
        @Suppress("UNCHECKED_CAST")
        return value as R
    }
}

/**
 * Helper class which can be used as a receiver to a block and adds a non-local
 * return function to the scope of the block.
 */
object NoInlineUnit {

    /**
     * Performs a non-local return.
     *
     * @throws NonLocalReturnException
     *          Always.
     */
    fun nonLocalReturn() {
        throw NonLocalReturnException(null)
    }
}

/**
 * Executes a block with non-local return support. If parameters are required
 * to the block, they should be placed in a closure.
 *
 * @param block
 *          The block to execute.
 * @return
 *          The local/non-local result of the block
 * @param R
 *          The return-type of the block.
 */
fun <R> noInlineNonLocalReturn(block : NoInline<R>.() -> R) : R {
    // Create the helper object
    val helper = NoInline<R>()

    return try {
        // Execute the block and return the local result if it completes normally
        helper.block()
    } catch (e : NonLocalReturnException) {
        // If a non-local return is performed, return the result of that
        helper.cast(e.value)
    }
}

/**
 * Executes a block with non-local return support. If parameters are required
 * to the block, they should be placed in a closure.
 *
 * @param block
 *          The block to execute.
 */
inline fun noInlineNonLocalReturn(block : NoInlineUnit.() -> Unit) {
    return try {
        // Execute the block with non-local return support
        NoInlineUnit.block()
    } catch (e : NonLocalReturnException) {
        // Just return if a non-local return is performed
    }
}
