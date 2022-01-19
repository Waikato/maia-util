package maia.util

/*
 * Utilities for working with lists.
 */

/**
 * A default implementation of [List.subList] which defers all call to
 * the original [list]. Suitable for [List]s which support random-access.
 *
 * @param list The source list.
 * @param fromIndex See [List.subList].
 * @param toIndex See [List.subList].
 */
class RandomAccessSubList<T>(
    private val list: List<T>,
    private val fromIndex: Int,
    private val toIndex: Int
): List<T> {
    init { ensureSublistRange(fromIndex, toIndex, list.size) {} }
    override val size : Int = toIndex - fromIndex
    override fun contains(element : T) : Boolean = indexOf(element) != -1
    override fun containsAll(elements : Collection<T>) : Boolean = elements.all { contains(it) }
    override fun get(index : Int) : T = ensureIndexInRange(index, size) { list[index + fromIndex] }
    override fun indexOf(element : T) : Int { for(index in fromIndex until toIndex) { if (element == list[index]) return index - fromIndex }; return -1 }
    override fun isEmpty() : Boolean = toIndex == fromIndex
    override fun iterator() : Iterator<T> = listIterator(0)
    override fun lastIndexOf(element : T) : Int { for (index in (fromIndex until toIndex).reversed()) { if (element == list[index]) return index - fromIndex }; return -1 }
    override fun listIterator() : ListIterator<T> = listIterator(0)
    override fun listIterator(index : Int) : ListIterator<T> = RandomAccessListIterator(this, index)
    override fun subList(fromIndex : Int, toIndex : Int) : List<T> = ensureSublistRange(fromIndex, toIndex, size) { list.subList(fromIndex + this.fromIndex, toIndex + this.fromIndex) }
}
