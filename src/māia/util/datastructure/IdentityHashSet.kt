package māia.util.datastructure

import māia.util.all
import māia.util.collect
import māia.util.map

/**
 * Mutable hash-set which store elements based on their identity rather
 * than there equality status.
 */
class IdentityHashSet<T>(elements : Iterator<T>) : MutableSet<T> {

    constructor(collection : Collection<T>) : this(collection.iterator())

    /** The map that backs this set, from identity to element. */
    private val intern = HashMap<Int, T>()

    // Add any constructor elements to the set
    init {
        for (element in elements)
            add(element)
    }

    override fun add(element : T) : Boolean {
        val identity = System.identityHashCode(element)
        val present = identity in intern
        intern[identity] = element
        return present
    }

    override fun addAll(elements : Collection<T>) : Boolean {
        return elements.map { add(it) }.toCollection(ArrayList()).any { it }
    }

    override fun clear() {
        intern.clear()
    }

    override fun iterator() : MutableIterator<T> {
        return intern.values.iterator()
    }

    override fun remove(element : T) : Boolean {
        val identity = System.identityHashCode(element)
        val present = identity in intern
        intern.remove(identity)
        return present
    }

    override fun removeAll(elements : Collection<T>) : Boolean {
        return elements.map { remove(it) }.toCollection(ArrayList()).any { it }
    }

    override fun retainAll(elements : Collection<T>) : Boolean {
        // Create a set of the identities to retain
        val retentionIdentities = elements
                .iterator()
                .map { System.identityHashCode(it) }
                .collect(HashSet())

        // Get a mutable iterator over ourself
        val internIdentityIterator = intern.keys.iterator()

        // Keep track of whether we actually remove any elements
        var modified = false

        // Go through all identities and remove those not in the retention set
        while (internIdentityIterator.hasNext()) {
            if (internIdentityIterator.next() !in retentionIdentities) {
                internIdentityIterator.remove()

                // Flag a modification as having occurred
                modified = true
            }
        }

        return modified
    }

    override val size : Int
        get() = intern.size

    override fun contains(element : T) : Boolean {
        return System.identityHashCode(element) in intern
    }

    override fun containsAll(elements : Collection<T>) : Boolean {
        return elements.iterator().all { contains(it) }
    }

    override fun isEmpty() : Boolean {
        return intern.isEmpty()
    }

    /**
     * Returns a copy of the set.
     *
     * @return
     *          The copy.
     */
    fun copy(): IdentityHashSet<T> {
        return IdentityHashSet(this)
    }

    /**
     * Returns a new [IdentityHashSet] which only contains those elements
     * that are in both this and another set.
     *
     * @param other     The other [IdentityHashSet] to create an intersection with.
     * @return          A new [IdentityHashSet] containing the elements common
     *                  to this and [other].
     */
    infix fun intersectionWith(other : IdentityHashSet<T>) : IdentityHashSet<T> {
        return copy().apply { retainAll(other) }
    }

    /**
     * Returns a new [IdentityHashSet] which only contains those elements
     * in this set that are not in the other set.
     *
     * @param other
     *          The other [IdentityHashSet] to compare to.
     * @return
     *          A new [IdentityHashSet] containing the elements common to
     *          this and [other].
     */
    infix fun differenceFrom(
        other: IdentityHashSet<T>
    ) : IdentityHashSet<T> {
        return copy().apply { removeAll(other) }
    }

    override fun toString() : String {
        return this.joinToString(
            prefix = (this::class.simpleName ?: IdentityHashSet::class.simpleName) + "(",
            postfix = ")"
        )
    }
}
