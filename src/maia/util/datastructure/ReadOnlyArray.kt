package maia.util.datastructure

import maia.util.RandomAccessListIterator
import maia.util.RandomAccessSubList

/**
 * Class which provides read-only access to an array.
 *
 * @param base The base array that is being viewed.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
@JvmInline
value class ReadOnlyArray<T>(
    private val base: Array<*>
): List<T> {

    /** See [Array.copyInto]. */
    fun copyInto(
        destination: Array<T>,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = size
    ): Array<T> = (base as Array<T>).copyInto(destination, destinationOffset, startIndex, endIndex)

    /* List methods */
    override val size: Int get() = base.size
    override fun get(index: Int): T = base[index] as T
    override fun iterator(): Iterator<T> = base.iterator() as Iterator<T>
    override fun contains(element : T) : Boolean = element in base
    override fun containsAll(elements : Collection<T>) : Boolean = elements.all { contains(it) }
    override fun indexOf(element : T) : Int = base.indexOf(element)
    override fun isEmpty() : Boolean = base.isEmpty()
    override fun lastIndexOf(element : T) : Int = base.lastIndexOf(element)
    override fun listIterator() : ListIterator<T> = listIterator(0)
    override fun listIterator(index : Int) : ListIterator<T> = RandomAccessListIterator(this, 0)
    override fun subList(fromIndex : Int, toIndex : Int) : List<T> = RandomAccessSubList(this, fromIndex, toIndex)
}

/**
 * Create a read-only view of an array.
 *
 * @receiver The source array.
 * @return The read-only view.
 */
fun <T> Array<T>.readOnly(): ReadOnlyArray<T> {
    return ReadOnlyArray(this)
}
