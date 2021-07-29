package māia.util

/*
 * Utilities for working with lambda blocks.
 */

/**
 * Helper function for identifying a block as a lambda,
 * in those cases where Kotlin can't determine this for
 * itself.
 *
 * @param block
 *          The body of the lambda function.
 * @return
 *          The lambda function.
 * @param F
 *          The type of the lambda function.
 */
fun <F : Function<*>> lambda(block : F) : F {
    return block
}

/**
 * Utility which converts the 'it' reference in a block to
 * an implicit 'this' reference.
 *
 * @param block     The block with an 'it' value.
 * @return          A block with an implicit 'this' reference.
 * @param T         The type of 'it'/'this'.
 * @param R         The return-type of the block.
 */
fun <T, R> itToThis(block : (T) -> R) : T.() -> R {
    return { block(this) }
}

/**
 * Utility which converts the implicit 'this' reference in a block to
 * an 'it' reference.
 *
 * @param block     The block with an implicit 'this' value.
 * @return          A block with an 'it' reference.
 * @param T         The type of 'it'/'this'.
 * @param R         The return-type of the block.
 */
fun <T, R> thisToIt(block : T.() -> R) : (T) -> R {
    return { it.block() }
}

/**
 * Returns a lambda which returns the receiver as a value
 * when called.
 *
 * @receiver    Any value.
 * @return      A lambda.
 */
fun <T> T.asBlock() : () -> T {
    return { this }
}

/**
 * Executes the given [block] and returns its result.
 *
 * @param block
 *          The block to evaluate.
 * @return
 *          The return value of the [block].
 */
fun <R> eval(block: () -> R): R = block()
