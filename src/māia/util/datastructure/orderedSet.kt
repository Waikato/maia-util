package māia.util.datastructure

/*
 * Defines the ordered set, an collection of distinct elements with a defined
 * order. Also defines an implementation based on the HashMap, and utilities
 * for working with ordered sets.
 */

import māia.util.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.min

/**
 * Interface for a set of ordered elements which doesn't support
 * duplicates.
 *
 * @param E
 *      The type of element in the ordered set.
 */
interface OrderedSet<E> : Set<E>, List<E> {

    override fun spliterator() : Spliterator<E>

}

/**
 * Interface for a mutable set of ordered elements which doesn't support
 * duplicates.
 *
 * @param E
 *      The type of element in the ordered set.
 */
interface MutableOrderedSet<E> : OrderedSet<E>, MutableSet<E>, MutableList<E> {

    override fun spliterator(): Spliterator<E>

}

/**
 * Implementation of the [MutableOrderedSet] interface which uses a [HashMap]
 * internally.
 *
 * TODO: Comments.
 *
 * @param initialValues
 *          The values to initialise the set with.
 * @param initialCapacity
 *          See [HashMap].
 * @param loadFactor
 *          See [HashMap].
 * @param E
 *          The type of element in the ordered set.
 */
class OrderedHashSet<E>(
        initialValues : Collection<E>? = null,
        initialCapacity : Int = 0,
        loadFactor : Float = 0.75f
) : MutableOrderedSet<E> {

    /**
     * Creates an [OrderedHashSet] containing the elements of the given
     * collection, and an initial capacity calculated from the collection's
     * size.
     *
     * @param initialValues
     *          The values to initialise the set with.
     * @param loadFactor
     *          See [HashMap].
     */
    constructor(
            initialValues : Collection<E>,
            loadFactor : Float = 0.75f
    ) : this(
            initialValues,
            kotlin.math.max(16, (initialValues.size / loadFactor).toInt() + 1),
            loadFactor
    )

    /**
     * Creates an [OrderedHashSet] containing the given elements.
     *
     * @param initialValues
     *          The values to initialise the set with.
     */
    constructor(vararg initialValues : E) : this(initialValues.asList())

    /** The mapping from element to index. */
    private val map = HashMap<E, Int>(initialCapacity, loadFactor)

    /** The ordered list of elements in the dataset. */
    private val list = ArrayList<E>(initialCapacity)

    /** The indices of the elements in the list which are incorrect in the map. */
    private var brokenIndices : IntRange? = null

    override val size: Int
        get() = list.size

    // Add the initial values
    init {
        if (initialValues != null) addAll(initialValues)
    }

    override fun contains(element: E): Boolean {
        return element in map
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return elements.all { it in map }
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun iterator(): MutableIterator<E> {
        return listIterator()
    }

    override fun get(index: Int) : E = list[index]

    override fun add(element: E): Boolean {
        return if (element !in map) {
            map[element] = list.size
            list.add(element)
        } else {
            false
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var result = false
        for (element in elements) {
            result = add(element) || result
        }
        return result
    }

    override fun clear() {
        map.clear()
        list.clear()
        brokenIndices = null
    }

    override fun remove(element: E) : Boolean {
        return if (element in map) {
            fixMap()
            val index = map.remove(element)!!
            list.removeAt(index)
            updateBrokenIndices(index)
            true
        } else {
            false
        }
    }

    override fun removeAll(elements: Collection<E>) : Boolean {
        val presentElements = elements.iterator().filter { it in map }.collect(ArrayList())

        if (presentElements.isEmpty()) return false

        fixMap()

        val indices = presentElements.iterator().map { map[it]!! }.collect(ArrayList())

        indices.sortDescending()

        indices.forEach { list.removeAt(it) }

        presentElements.forEach { map.remove(it) }

        updateBrokenIndices(indices.last())

        return true
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun spliterator(): Spliterator<E> {
        TODO("Not yet implemented")
    }

    override fun indexOf(element: E): Int {
        fixMap()
        return map[element] ?: -1
    }

    override fun lastIndexOf(element: E): Int {
        return indexOf(element)
    }

    override fun listIterator(): MutableListIterator<E> {
        return listIterator(0)
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        // TODO: Check index in range
        return OrderedHashSetIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): OrderedHashSet<E> {
        TODO("Not yet implemented")
    }

    override fun add(index: Int, element: E) {
        if (element !in map) {
            list.add(index, element)
            map[element] = index
            updateBrokenIndices(index)
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val toAdd = elements.iterator().filter { it !in map }.collect(ArrayList())

        if (toAdd.isEmpty()) return false

        list.addAll(index, toAdd)

        toAdd.forEach { map[it] = 0 }

        updateBrokenIndices(index)

        return true
    }

    override fun removeAt(index: Int): E {
        val result = list.removeAt(index)

        updateBrokenIndices(index)

        return result
    }

    override fun set(index: Int, element: E): E {
        if (this[index] == element) return element
        val removed = removeAt(index)
        when {
            element !in this -> {
                add(index, element)
            }
            index == this.size -> {
                remove(element)
                add(element)
            }
            this[index] != element -> {
                remove(element)
                add(index, element)
            }
        }
        return removed
    }

    override fun equals(other : Any?) : Boolean {
        return other is OrderedSet<*>
                && iterablesAreEquivalent(this, other, EQUALITY_EQUIVALENCY::test)
    }

    override fun hashCode() : Int {
        return list.hashCode()
    }

    override fun toString() : String {
        return list.joinToString(prefix = "{", postfix = "}")
    }

    /**
     * Fixes the indices in the map so that they correspond to the current
     * state of the ordering.
     */
    private fun fixMap() {
        brokenIndices?.forEach { map[list[it]] = it }
        brokenIndices = null
    }

    /**
     * Updates the range of broken indices to cover the given index.
     *
     * @param index
     *          The index that is now considered broken (along with all
     *          subsequent indices).
     */
    private fun updateBrokenIndices(index : Int) {
        brokenIndices = min(brokenIndices?.first ?: index, index) until list.size
    }

    /**
     * An iterator over this ordered hash-set.
     *
     * TODO: Add support for quickly detecting and failing on concurrent
     *       modification.
     *
     * @param start
     *          The index to start iterating from.
     */
    inner class OrderedHashSetIterator(
            start : Int
    ) : MutableListIterator<E> {

        /** The iterator's cursor. */
        private var index = start

        /** Whether the last item returned was from [next]. */
        private var nextCalledLast = false

        /** Whether the last item returned was from [previous]. */
        private var previousCalledLast = false

        /** The index of the last returned element from [next]/[previous]. */
        private val lastIndex : Int
            get() {
                return when {
                    nextCalledLast -> index
                    previousCalledLast -> index
                    else -> throw IllegalStateException(
                            "Can't modify last returned value before a call to" +
                                    "next or previous has been made"
                    )
                }
            }

        override fun hasPrevious() : Boolean {
            return index > 0
        }

        override fun previous() : E = ensureHasPrevious {
            nextCalledLast = false
            previousCalledLast = true
            return this@OrderedHashSet[index--]
        }

        override fun previousIndex() : Int {
            return index - 1
        }

        override fun hasNext() : Boolean {
            return index < this@OrderedHashSet.size
        }

        override fun next() : E = ensureHasNext {
            previousCalledLast = false
            nextCalledLast = true
            return this@OrderedHashSet[index++]
        }

        override fun nextIndex() : Int {
            return index
        }

        override fun add(element : E) = discard {
            this@OrderedHashSet.set(index++, element)
        }

        override fun remove() {
            this@OrderedHashSet.removeAt(lastIndex)
            previousCalledLast = false
            nextCalledLast = false
        }

        override fun set(element : E) {
            this@OrderedHashSet.set(lastIndex, element)
            previousCalledLast = false
            nextCalledLast = false
        }

    }
}

/**
 * Builds an ordered set.
 *
 * @param block
 *          The builder function, which treats the ordered set as mutable.
 * @return
 *          The immutable ordered set.
 * @param E
 *          The type of element in the ordered set.
 */
fun <E> buildOrderedSet(block : MutableOrderedSet<E>.() -> Unit) : MutableOrderedSet<E> {
    return OrderedHashSet<E>().apply(block)
}

/**
 * Returns an immutable view of a possibly-mutable ordered set.
 *
 * @receiver
 *          The possibly-mutable ordered set to view.
 * @param ensureImmutable
 *          Whether to create an immutable view even if the receiver does not
 *          implement [MutableOrderedSet].
 * @return
 *          The immutable view.
 * @param E
 *          The type of element in the ordered set.
 */
fun <E> OrderedSet<E>.asOrderedSet(ensureImmutable : Boolean = false) : OrderedSet<E> {
    return if (ensureImmutable || this is MutableOrderedSet)
        object : OrderedSet<E> by this {}
    else
        this
}

