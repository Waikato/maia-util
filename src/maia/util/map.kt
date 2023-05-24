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

/**
 * Formats a map as a string. The default parameter values give a compressed
 * form:
 *
 * ```
 * {key1:value1,key2:value2,...}
 * ```
 *
 * @receiver
 *          The map to format.
 * @param prefix
 *          The opening brace.
 * @param suffix
 *          The closing brace.
 * @param keyValueSeparator
 *          The separator to place between each key and value.
 * @param entrySeparator
 *          The separator to place between each key/value pair.
 * @param formatKey
 *          A function which formats each key to a string.
 * @param formatValue
 *          A function which formats each value to a string.
 * @return
 *          A formatted string-representation of the map.
 */
fun <K, V> Map<K, V>.format(
    prefix: String = "{",
    suffix: String = "}",
    keyValueSeparator: String = ":",
    entrySeparator: String = ",",
    formatKey: (K) -> String = { it.toString() },
    formatValue: (V) -> String = { it.toString() }
): String {
    return "${prefix}${this.entries.map { "${formatKey(it.key)}${keyValueSeparator}${formatValue(it.value)}" }.joinToString(entrySeparator)}${suffix}"
}

/**
 * Formats a map as a string, using a standard "pretty" form:
 *
 * ```
 * {
 *      key1: value1,
 *      key2: value2,
 *      ...
 * }
 * ```
 *
 * @receiver
 *          The map to format.
 * @param formatKey
 *          A function which formats each key to a string.
 * @param formatValue
 *          A function which formats each value to a string.
 * @return
 *          A formatted string-representation of the map.
 */
fun <K, V> Map<K, V>.formatPretty(
    formatKey: (K) -> String = { it.toString() },
    formatValue: (V) -> String = { it.toString() }
) = format(
    "{\n\t",
    "\n}",
    ": ",
    ",\n\t",
    formatKey,
    formatValue
)

/**
 * Creates a read-only view of a map.
 *
 * @receiver
 *          The map to view.
 * @return
 *          The read-only view of the map.
 */
fun <K, V> Map<K, V>.readOnly(): Map<K, V> {
    return object: Map<K, V> by this {}
}
