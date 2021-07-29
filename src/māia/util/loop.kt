package māia.util

/**
 * Closed type that can be thrown to control the iteration of the inline
 * for-loops below. Provides equivalent functionality to 'continue' and
 * 'break'.
 */
sealed class LoopControl: Exception() {
    /** Throw this to continue the loop. */
    object Continue : LoopControl()

    /** Throw this to break the loop. */
    object Break : LoopControl()
}

/**
 * A traditional for-loop with a setup, condition and update block.
 *
 * @param setup
 *          The setup of the for-loop's state.
 * @param condition
 *          This condition must be true at the beginning of each iteration
 *          for it to go ahead.
 * @param update
 *          Updates the for-loop's at the end of each iteration.
 * @param block
 *          The body of the loop.
 */
inline fun <S> inlineForLoop(
    crossinline setup: () -> S,
    crossinline condition: (S) -> Boolean,
    crossinline update: (S) -> S,
    block: (S) -> Unit
) {
    var sentinel = setup()
    forLoop@while(condition(sentinel)) {
        try {
            block(sentinel)
        } catch (e: LoopControl) {
            when (e) {
                is LoopControl.Continue -> continue@forLoop
                is LoopControl.Break -> break@forLoop
            }
        }
        sentinel = update(sentinel)
    }
}

/**
 * A traditional for-loop with a condition and update block.
 *
 * @param condition
 *          This condition must be true at the beginning of each iteration
 *          for it to go ahead.
 * @param update
 *          Updates the for-loop's at the end of each iteration.
 * @param block
 *          The body of the loop.
 */
inline fun inlineForLoop(
    crossinline condition: () -> Boolean,
    crossinline update: () -> Unit,
    block: () -> Unit
) {
    inlineForLoop<Unit>({}, { condition() }, { update() }, { block() })
}

/**
 * Basic for-loop over integers from 0 to [end], at [step] intervals.
 *
 * @param end
 *          Once the index reaches this value, end iteration.
 * @param step
 *          The amount to increment the index by each iteration.
 * @param block
 *          The body of the iteration.
 */
inline fun inlineRangeForLoop(
    end: Int,
    step: Int = if (end < 0) -1 else 1,
    block: (Int) -> Unit
) {
    inlineRangeForLoop(0, end, step, block)
}

/**
 * Basic for-loop over integers from [start] to [end], at [step] intervals.
 *
 * @param start
 *          The starting value of the index.
 * @param end
 *          Once the index reaches this value, end iteration.
 * @param step
 *          The amount to increment the index by each iteration.
 * @param block
 *          The body of the iteration.
 */
inline fun inlineRangeForLoop(
    start: Int,
    end: Int,
    step: Int = if (end < start) -1 else 1,
    block: (Int) -> Unit
) {
    inlineForLoop({ start }, { it < end }, { it + step }, block)
}
