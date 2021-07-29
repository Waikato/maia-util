package māia.util

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
