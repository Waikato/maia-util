package maia.util

/*
 * Implementation of collection builders so no opt-in is required.
 *
 * TODO: Ensure mutable accessors of underlying mutable types (e.g. MutableIterator)
 *       return immutable views also.
 */

/**
 * Builds a set.
 *
 * @param block
 *          A series of actions on a mutable set.
 * @return
 *          The resulting set.
 * @param E
 *          The type of value in the set.
 */
fun <T> buildSet(block : MutableSet<T>.() -> Unit) : Set<T> {
    return object : Set<T> by HashSet<T>().apply(block) {}
}

/**
 * Builds a list.
 *
 * @param block
 *          A series of actions on a mutable list.
 * @return
 *          The resulting list.
 * @param E
 *          The type of value in the list.
 */
fun <E> buildList(block : MutableList<E>.() -> Unit) : List<E> {
    return object : List<E> by ArrayList<E>().apply(block) {}
}

/**
 * Builds a map.
 *
 * @param block
 *          A series of actions on a mutable map.
 * @return
 *          The resulting map.
 * @param K
 *          The type of key in the map.
 * @param V
 *          The type of value in the map.
 */
fun <K, V> buildMap(block : MutableMap<K, V>.() -> Unit) : Map<K, V> {
    return object : Map<K, V> by HashMap<K, V>().apply(block) {}
}
