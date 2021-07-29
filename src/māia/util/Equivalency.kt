package māia.util

/**
 * Interface representing an external equivalency over type T.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
fun interface Equivalency<in T> {

    /**
     * Tests whether [a] and [b] are equivalent under this equivalency.
     *
     * @param a
     *          The first instance.
     * @param b
     *          The second instance.
     * @return
     *          If [a] and [b] are equivalent.
     */
    fun test(a: T, b: T): Boolean
}

/** Equivalency on a == b. */
val EQUALITY_EQUIVALENCY: Equivalency<Any?> = Equivalency { a, b -> a == b }

/** Equivalency on a === b. */
val IDENTITY_EQUIVALENCY: Equivalency<Any?> = Equivalency { a, b -> a === b }
