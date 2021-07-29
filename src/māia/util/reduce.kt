package māia.util

/**
 * Functional interface for a reducer function.
 *
 * @param S
 *          The type of the reduction accumulator.
 * @param T
 *          The type of the values being reduced.
 */
fun interface Reducer<S, T : S> {
    /**
     * Reducer function.
     *
     * @param state
     *          The current state of the reduction.
     * @param value
     *          The next value to reduce.
     * @return
     *          The next state of the reduction.
     */
    fun reduce(state: S, value: T): S
}

/**
 * Creates a reducer which reduces to the maximum value.
 */
fun <T: Comparable<T>> maxReducer(): Reducer<T, T> = Reducer {
        state, value ->
    if (state > value) state else value
}

val INT_SUM_REDUCER: Reducer<Int, Int> = Reducer {
        state, value ->
    state + value
}

val LONG_SUM_REDUCER: Reducer<Long, Long> = Reducer {
        state, value ->
    state + value
}
