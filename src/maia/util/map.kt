package maia.util

/*
 * Utilities for working with maps.
 */

/**
 * Sets the value for the given key to the result of the given block,
 * unless a value already exists for the key. Returns the value for the
 * key after calling this function (i.e. the presiding value or the result
 * of the block).
 *
 * @receiver        A mutable map.
 * @param key       The key to check.
 * @param block     An initialiser for the value if not already present.
 * @return          The value for the key after this function has returned.
 * @param K         The key type.
 * @param V         The value type.
 */
fun <K, V> MutableMap<K, V>.currentOrSet(key : K, block : () -> V) : V {
    return if (key in this) {
        @Suppress("UNCHECKED_CAST")
        this[key] as V
    } else {
        val blockResult = block()
        this[key] = blockResult
        blockResult
    }
}

/**
 * Performs the given action on the value corresponding to the
 * given [key], if it is present.
 *
 * @param key The key to inspect.
 * @param block The action to (potentially) perform on the value at [key].
 *
 * @return Whether the action was performed.
 */
inline fun <K, V> Map<K, V>.doIfPresent(
    key: K,
    block: (V) -> Unit
): Boolean {
    if (key in this) {
        @Suppress("UNCHECKED_CAST")
        block(this[key] as V)
        return true
    }

    return false
}
