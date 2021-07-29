package māia.util

/*
 * Definitions of singleton empty versions of common types.
 */

/**
 * A typed empty iterator.
 */
object EmptyIterator : Iterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
}

/**
 * A typed empty list.
 */
object EmptyList : List<Nothing> {
    override val size : Int = 0
    override fun contains(element : Nothing) : Boolean = false
    override fun containsAll(elements : Collection<Nothing>) : Boolean = elements.isEmpty()
    override fun get(index : Int) : Nothing = throw IndexOutOfBoundsException(index)
    override fun indexOf(element : Nothing) : Int = -1
    override fun isEmpty() : Boolean = true
    override fun iterator() : Iterator<Nothing> = EmptyIterator
    override fun lastIndexOf(element : Nothing) : Int = -1
    override fun listIterator() : ListIterator<Nothing> = EmptyListIterator
    override fun listIterator(index : Int) : ListIterator<Nothing> = EmptyListIterator
    override fun subList(fromIndex : Int, toIndex : Int) : List<Nothing> {
        if (fromIndex != 0) throw IndexOutOfBoundsException(fromIndex)
        if (toIndex != 0) throw IndexOutOfBoundsException(toIndex)
        return this
    }
}

/**
 * A typed empty list-iterator.
 */
object EmptyListIterator : ListIterator<Nothing> {
    override fun hasNext() : Boolean = false
    override fun hasPrevious() : Boolean = false
    override fun next() : Nothing = throw NoSuchElementException()
    override fun nextIndex() : Int = 0
    override fun previous() : Nothing = throw NoSuchElementException()
    override fun previousIndex() : Int = -1
}

/**
 * A typed empty set.
 */
object EmptySet : Set<Nothing> {
    override val size : Int = 0
    override fun contains(element : Nothing) : Boolean = false
    override fun containsAll(elements : Collection<Nothing>) : Boolean = elements.isEmpty()
    override fun isEmpty() : Boolean = true
    override fun iterator() : Iterator<Nothing> = EmptyIterator
}
