package māia.util

/*
 * Defines types for collections of on/off flag states and methods
 * for working with them.
 */

/**
 * Interface for objects which represent a set of on/off flags.
 */
interface Flags {

    /** The number of flags in this set of flags. */
    val numFlags : Int

    /**
     * Gets the state of the flag at the given position.
     *
     * @param flag
     *          The index of the flag to check.
     * @return
     *          The state of the selected flag.
     * @throws IndexOutOfBoundsException
     *          If [flag] is not in [0,[numFlags]).
     */
    operator fun get(flag : Int) : Boolean

}

/**
 * Interface for objects which represent a set of on/off flags
 * that can be set/cleared.
 */
interface MutableFlags : Flags {

    /**
     * Sets the state of the specified flag.
     *
     * @param flag
     *          The flag to set.
     * @param state
     *          The state to set the flag to.
     */
    operator fun set(flag : Int, state : Boolean)

}

/**
 * Implementation of [MutableFlags] which stores the flag states in the bits
 * of an array of integers.
 *
 * @throws IllegalArgumentException
 *          If [numFlags] is negative.
 */
class IntBitFlags(override val numFlags : Int) : MutableFlags {

    init {
        // Make sure the number of flags isn't negative
        if (numFlags < 0) throw IllegalArgumentException("numFlags ($numFlags) can't be negative")
    }

    /** Array of integers holding the flag states. */
    private val stateArray = IntArray((numFlags - 1) / Int.SIZE_BITS + 1) { 0 }

    /** The range of valid flag indices. */
    val range : IntRange = 0 until numFlags

    override operator fun get(flag : Int) : Boolean = withIndices(flag) { stateIndex, bitIndex ->
        return stateArray[stateIndex].bitState(bitIndex)
    }

    override operator fun set(flag : Int, state : Boolean) = withIndices(flag) { stateIndex, bitIndex ->
        stateArray[stateIndex] = stateArray[stateIndex].modifyBit(bitIndex, state)
    }

    /**
     * Handles making sure the flag index is valid and dividing into an array index
     * and a bit index.
     *
     * @param flag
     *          The flag index to handle.
     * @param block
     *          The action to perform with the state/bit indices.
     * @return
     *          The result of the [block].
     * @throws IndexOutOfBoundsException
     *          If [flag] is not in [0,[numFlags]).
     */
    private inline fun <R> withIndices(flag : Int, block : (Int, Int) -> R) : R = withCheckedIndex(flag, range) {
        return block(flag / Int.SIZE_BITS, flag % Int.SIZE_BITS)
    }

}

/**
 * Uses an underlying flags object to represent an enumeration as a set of flags.
 *
 * @param base
 *          The underlying [Flags] object storing the state of the flags.
 * @param values
 *          The enumeration values.
 * @param E
 *          The type of enumeration to use.
 */
open class EnumFlags<E : Enum<E>> protected constructor(
        protected val base : Flags,
        val values : Array<E>
) {
    init {
        // Make sure the base flags have a flag for each value
        if (base.numFlags != values.size) throw IllegalArgumentException(
                "size of underlying flags must match enum size exactly"
        )
    }

    /** The number of flags in this set of flags. */
    val numFlags : Int = values.size

    /**
     * Gets the state of the flag at the given position.
     *
     * @param flag
     *          The index of the flag to check.
     * @return
     *          The state of the selected flag.
     * @throws IndexOutOfBoundsException
     *          If [flag] is not in [0,[numFlags]).
     */
    operator fun get(flag : E) : Boolean = base[flag.ordinal]

    companion object {

        /**
         * Creates an [EnumFlags] for the reified enumeration.
         *
         * @param base
         *          The underlying [Flags] object storing the state of the flags.
         * @return
         *          The created [EnumFlags] instance.
         * @param E
         *          The type of enumeration to use.
         */
        inline fun <reified E : Enum<E>> create(base : Flags) : EnumFlags<E> {
            return EnumFlags(base, enumValues())
        }

    }

}

/**
 * Uses an underlying flags object to represent an enumeration as a set of
 * mutable flags.
 *
 * @param base
 *          The underlying [MutableFlags] object storing the state of the flags.
 * @param values
 *          The enumeration values.
 * @param E
 *          The type of enumeration to use.
 */
open class MutableEnumFlags<E : Enum<E>> protected constructor(
        base : MutableFlags,
        values : Array<E>
) : EnumFlags<E>(base, values) {

    /** The base flags cast to its (definitely) mutable type. */
    private val castBase : MutableFlags
        get() = base as MutableFlags

    /**
     * Sets the state of the specified flag.
     *
     * @param flag
     *          The flag to set.
     * @param state
     *          The state to set the flag to.
     */
    operator fun set(flag : E, state : Boolean) {
        castBase[flag.ordinal] = state
    }

    companion object {

        /**
         * Creates a [MutableEnumFlags] for the reified enumeration.
         *
         * @param base
         *          The underlying [MutableFlags] object storing the state of the flags.
         * @return
         *          The created [MutableEnumFlags] instance.
         * @param E
         *          The type of enumeration to use.
         */
        inline fun <reified E : Enum<E>> create(base : MutableFlags) : MutableEnumFlags<E> {
            return MutableEnumFlags(base, enumValues())
        }

    }

}

/**
 * Formats the current state of flags as a string.
 *
 * @param separator
 *          The sequence to separate flag states.
 * @param prefix
 *          The sequence to place before the flag states.
 * @param postfix
 *          The sequence to place after the flag states.
 * @param limit
 *          The maximum number of flag states to format.
 * @param truncated
 *          The sequence to indicate the [limit] was reached.
 * @param transform
 *          Function to format the sequence for the flag index/state.
 * @return
 *          The formatted string.
 */
fun Flags.joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: (Int, Boolean) -> CharSequence = { _, state ->
            if (state) "on" else "off"
        }
) : String {
    // Format the actual transform lambda to pass to joinToString
    val actualTransform = { index : Int ->
        transform(index, this[index])
    }

    return indexIterator(numFlags).joinToString(
            separator, prefix, postfix, limit, truncated, actualTransform
    )
}

/**
 * Formats the current state of flags as a string.
 *
 * @param separator
 *          The sequence to separate flag states.
 * @param prefix
 *          The sequence to place before the flag states.
 * @param postfix
 *          The sequence to place after the flag states.
 * @param limit
 *          The maximum number of flag states to format.
 * @param truncated
 *          The sequence to indicate the [limit] was reached.
 * @param transform
 *          Function to format the sequence for the flag index/name/state.
 * @return
 *          The formatted string.
 */
fun <E : Enum<E>> EnumFlags<E>.joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: (Int, E, Boolean) -> CharSequence = { _, flag, state ->
            if (state) flag.name else "!${flag.name}"
        }
) : String {
    // Format the actual transform lambda to pass to joinToString
    val actualTransform = { index : Int ->
        val flag = values[index]
        transform(index, flag, this[flag])
    }

    return indexIterator(numFlags).joinToString(
            separator, prefix, postfix, limit, truncated, actualTransform
    )
}
