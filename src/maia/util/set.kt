package maia.util

/*
 * Package for utilities for working with sets.
 */

/**
 * Checks if a set is a sub-set of another.
 *
 * @receiver        The set to check for sub-set status.
 * @param other     The set to check the receiver against.
 * @return          True if the receiver is a sub-set of [other],
 *                  false if not.
 */
infix fun <T> Set<T>.isSubSetOf(other : Collection<T>) : Boolean {
    return all { it in other }
}

/**
 * Checks if a set is not a sub-set of another.
 *
 * @receiver        The set to check for sub-set status.
 * @param other     The set to check the receiver against.
 * @return          False if the receiver is a sub-set of [other],
 *                  true if not.
 */
infix fun <T> Set<T>.isNotSubSetOf(other : Collection<T>) : Boolean {
    return !(this isSubSetOf other)
}

/**
 * Extracts elements from a set that match a predicate into
 * another set.
 *
 * @receiver            The set to extract elements from.
 * @param predicate     The condition for elements that should be extracted.
 * @return              A set of the extracted elements.
 */
fun <T> Set<T>.extract(predicate : (T) -> Boolean) : Set<T> {
    return iterator().filter(predicate).collect(HashSet())
}

/**
 * Extracts elements from a set that match a predicate into
 * another set. Optionally removes the extracted elements from
 * this set.
 *
 * @receiver            The set to extract elements from.
 * @param remove        Whether to remove the elements from this set.
 * @param predicate     The condition for elements that should be extracted.
 * @return              A set of the extracted elements.
 */
fun <T> MutableSet<T>.extract(remove : Boolean = false, predicate : (T) -> Boolean) : Set<T> {
    // Extract the elements
    val resultSet = extract(predicate)

    // Optionally remove them from this set
    if (remove) this.removeAll(resultSet)

    return resultSet
}
