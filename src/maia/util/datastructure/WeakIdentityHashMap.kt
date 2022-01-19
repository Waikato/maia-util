package maia.util.datastructure

import maia.util.*
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import kotlin.UnsupportedOperationException
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * A hash-map implementation which weakly refers to its keys by
 * identity, rather than their hash. This way each unique key-object
 * can have a separate value, rather than all "equal" objects sharing
 * a value. Useful for extension delegate properties where the value
 * of the property is not necessarily equal for equal objects.
 */
class WeakIdentityHashMap<K : Any, V> : MutableMap<K, V> {

    /** Map from weak-ref to object identity. */
    private val refToID = HashMap<WeakReference<K>, Int>()

    /** Map from object identity to weak-ref. */
    private val idToRef = HashMap<Int, WeakReference<K>>()

    /** Map from key identity to value. */
    private val idToValue = HashMap<Int, V>()

    /** Queue of collected objects. */
    private val refQueue = ReferenceQueue<K>()

    override val size : Int
        get() = cleaned {
            // Count the number of live entries
            var size = 0
            forEachLiveEntry { _, _, _ -> size += 1 }
            return size
        }

    override fun isEmpty() : Boolean = cleaned {
        // We're not empty if we have any live entries
        forEachLiveEntry { _, _, _ ->
            return false
        }

        return true
    }

    override fun containsKey(key : K) : Boolean {
        return getValue(key) !is Absent
    }

    override fun containsValue(value : V) : Boolean = cleaned {
        // If any live entry has the provided value, then we contain the value
        forEachLiveEntry { _, _, liveValue ->
            if (value == liveValue)
                return true
        }

        return false
    }

    override fun get(key : K) : V? {
        return getValue(key).getOrNull()
    }

    /**
     * Gets a value from the map under the given key, if one is present.
     *
     * @param key   The key to get a value for.
     * @return      The optionally-present value from the map.
     */
    fun getValue(key : K) : Optional<V> = cleaned {
        // Get the ID of the key
        val id = System.identityHashCode(key)

        // If we don't have a reference for the key, we don't have a value either
        val ref = idToRef[id] ?: return Absent

        // If the reference is dead, we don't have a value for the key
        if (ref.get() == null) {
            removeID(id) // Remove the dead reference
            return Absent
        }

        return Present(idToValue[id] as V)
    }

    override val entries : MutableSet<MutableMap.MutableEntry<K, V>> = EntrySet()
        get() = cleaned { field }

    override val keys : MutableSet<K> = KeySet()
        get() = cleaned { field }

    override val values : MutableCollection<V> = ValueCollection()
        get() = cleaned { field }

    override fun clear() {
        refToID.clear()
        idToRef.clear()
        idToValue.clear()
    }

    override fun put(key : K, value : V) : V? = cleaned {
        return putUncleaned(key, value)
    }

    override fun putAll(from : Map<out K, V>) = cleaned {
        for ((key, value) in from.entries) {
            putUncleaned(key, value)
        }
    }

    override fun remove(key : K) : V? = cleaned {
        val id = System.identityHashCode(key)
        return removeID(id).getOrNull()
    }

    /**
     * Removes all entries from this map with keys in the given collection.
     *
     * @param keys  The keys to remove.
     * @return      Whether the map was modified.
     */
    fun removeAll(keys : Collection<K>) : Boolean {
        return keys
                .iterator()
                .map { System.identityHashCode(it) }
                .map { removeID(it) }
                .map { it !is Absent }
                .asIterable()
                .reduce { anyRemoved, wasRemoved -> anyRemoved || wasRemoved }
    }

    /**
     * Removes a single entry from this map with the given value.
     *
     * @param value     The value to remove.
     * @return          Whether an entry was found and removed.
     */
    fun removeValue(value : V) : Boolean = cleaned {
        this@WeakIdentityHashMap.forEachLiveEntry { id, _, liveValue ->
            if (liveValue == value) {
                this@WeakIdentityHashMap.removeID(id)
                return true
            }
        }
        return false
    }

    /**
     * Removes all entries from this map which have values in the given collection.
     *
     * @param values    The values to remove from the map.
     * @return          Whether any entries were removed from the map.
     */
    fun removeAllValues(values : Collection<V>) : Boolean = cleaned {
        // Keep a list of IDs to remove so we don't concurrently modify the map
        val idsToRemove = ArrayList<Int>()

        // Create a list of all entries which have values in the collection
        this@WeakIdentityHashMap.forEachLiveEntry { id, _, value ->
            if (value in values) {
                idsToRemove.add(id)
            }
        }

        // Remove the identified entries
        for (id in idsToRemove) this@WeakIdentityHashMap.removeID(id)

        return idsToRemove.size > 0
    }

    /**
     * Removes all entries from this map which have values not in the
     * given collection.
     *
     * @param values    The values to keep in the map.
     * @return          Whether any entries were removed from the map.
     */
    fun retainAllValues(values : Collection<V>) : Boolean = cleaned {
        // Keep a list of IDs to remove so we don't concurrently modify the map
        val idsToRemove = ArrayList<Int>()

        // Create a list of all entries which have values not in the collection
        this@WeakIdentityHashMap.forEachLiveEntry { id, _, value ->
            if (value !in values) {
                idsToRemove.add(id)
            }
        }

        // Remove the identified entries
        for (id in idsToRemove) this@WeakIdentityHashMap.removeID(id)

        return idsToRemove.size > 0
    }

    /**
     * Removes all entries from this map that are not in the given collection.
     *
     * @param keys  The keys to retain.
     * @return      Whether the map was modified.
     */
    fun retainAll(keys : Collection<K>) : Boolean = cleaned {
        // Create a set of the IDs of the keys to keep
        val idSet = keys
                .iterator()
                .map { System.identityHashCode(it) }
                .collect(HashSet())

        // Get an iterator over the entries of the map
        val iter = HoldingIterator()

        // Iterate and remove entries that are not in the set
        var modified = false
        while (iter.hasNext()) {
            val next = iter.next()
            if (next.first !in idSet) {
                iter.remove()
                modified = true
            }
        }

        return modified
    }

    override fun toString() : String {
        return HoldingIterator().joinToString(prefix = "{", postfix = "}") { (id, key) ->
            "$key[id=$id]: ${idToValue[id]!!}"
        }
    }

    /**
     * Iterates through the entries in the map, calling the provided block on each
     * live one found.
     *
     * @param block     The function to call for each live entry.
     */
    private inline fun forEachLiveEntry(block: (Int, WeakReference<K>, V) -> Unit) {
        HoldingIterator().forEach { (id, _) ->
            block(id, idToRef[id]!!, idToValue[id]!!)
        }
    }

    /**
     * Removes an item from the map by ID.
     *
     * @param id    The ID of the item to remove.
     * @return      The value previously associated with the ID.
     */
    private fun removeID(id : Int) : Optional<V> {
        // Remove the reference for the ID, and if there is none,
        // there is also no value
        val ref = idToRef.remove(id) ?: return Absent

        // Remove the value for the ID
        val value = idToValue.remove(id)

        // Remove the ID from the reference-map as well
        refToID.remove(ref)

        // Return the value only if the reference is still live
        return if (ref.get() == null) Absent else Present(value as V)
    }

    /**
     * Adds a value to the map without cleaning first.
     *
     * @param key       The key to put the value under.
     * @param value     The value.
     * @return          The previous value in the map, or null if there
     *                  was no previous value.
     */
    private fun putUncleaned(key: K, value : V) : V? {
        // Get the identity of the key
        val id = System.identityHashCode(key)

        // If we don't have a live reference for this key, create one
        if (idToRef[id]?.get() == null) {
            val ref = WeakReference(key, refQueue)
            refToID[ref] = id
            idToRef[id] = ref
            idToValue.remove(id)
        }

        return idToValue.put(id, value)
    }

    /**
     * Removes any dead references from the map.
     */
    private fun clean() {
        while (true) {
            // Get the next reference from the queue, or break if finished
            val reference = refQueue.poll() ?: break

            // Get the object identity this reference refers to, or skip if
            // already removed (stale reference)
            val identity = refToID.remove(reference) ?: continue

            // Remove the entry from all other component maps
            idToRef.remove(identity)
            idToValue.remove(identity)
        }
    }

    /**
     * Executes the provided block in the context of a cleaned state.
     *
     * @param block     The block to execute.
     * @return          The result of the block.
     */
    private inline fun <R> cleaned(block : () -> R) : R {
        clean()
        return block()
    }

    private inner class KeySet : MutableSet<K> {
        override val size : Int get() = this@WeakIdentityHashMap.size
        override fun add(element : K) : Boolean = throw UnsupportedOperationException()
        override fun addAll(elements : Collection<K>) : Boolean = throw UnsupportedOperationException()
        override fun clear() = this@WeakIdentityHashMap.clear()
        override fun iterator() : MutableIterator<K> = HoldingKeyIterator()
        override fun remove(element : K) : Boolean = this@WeakIdentityHashMap.removeID(System.identityHashCode(element)) !is Absent
        override fun removeAll(elements : Collection<K>) : Boolean = this@WeakIdentityHashMap.removeAll(elements)
        override fun retainAll(elements : Collection<K>) : Boolean = this@WeakIdentityHashMap.retainAll(elements)
        override fun contains(element : K) : Boolean = this@WeakIdentityHashMap.containsKey(element)
        override fun containsAll(elements : Collection<K>) : Boolean = elements.all { contains(it) }
        override fun isEmpty() : Boolean = this@WeakIdentityHashMap.isEmpty()
    }

    private inner class ValueCollection : MutableCollection<V> {
        override val size : Int = this@WeakIdentityHashMap.size
        override fun contains(element : V) : Boolean = this@WeakIdentityHashMap.containsValue(element)
        override fun containsAll(elements : Collection<V>) : Boolean = elements.all { contains(it) }
        override fun isEmpty() : Boolean = this@WeakIdentityHashMap.isEmpty()
        override fun add(element : V) : Boolean = throw UnsupportedOperationException()
        override fun addAll(elements : Collection<V>) : Boolean = throw UnsupportedOperationException()
        override fun clear() = this@WeakIdentityHashMap.clear()
        override fun iterator() : MutableIterator<V> = HoldingValueIterator()
        override fun remove(element : V) : Boolean = this@WeakIdentityHashMap.removeValue(element)
        override fun removeAll(elements : Collection<V>) : Boolean = this@WeakIdentityHashMap.removeAllValues(elements)
        override fun retainAll(elements : Collection<V>) : Boolean = this@WeakIdentityHashMap.retainAllValues(elements)
    }

    private inner class EntrySet : MutableSet<MutableMap.MutableEntry<K, V>> {
        override val size : Int = this@WeakIdentityHashMap.size
        override fun add(element : MutableMap.MutableEntry<K, V>) : Boolean = throw UnsupportedOperationException()
        override fun addAll(elements : Collection<MutableMap.MutableEntry<K, V>>) : Boolean = throw UnsupportedOperationException()
        override fun clear() = this@WeakIdentityHashMap.clear()
        override fun iterator() : MutableIterator<MutableMap.MutableEntry<K, V>> = HoldingEntryIterator()
        override fun remove(element : MutableMap.MutableEntry<K, V>) : Boolean = throw UnsupportedOperationException()
        override fun removeAll(elements : Collection<MutableMap.MutableEntry<K, V>>) : Boolean = throw UnsupportedOperationException()
        override fun retainAll(elements : Collection<MutableMap.MutableEntry<K, V>>) : Boolean = throw UnsupportedOperationException()
        override fun contains(element : MutableMap.MutableEntry<K, V>) : Boolean = throw UnsupportedOperationException()
        override fun containsAll(elements : Collection<MutableMap.MutableEntry<K, V>>) : Boolean = throw UnsupportedOperationException()
        override fun isEmpty() : Boolean = this@WeakIdentityHashMap.isEmpty()
    }

    /**
     * Represents a single entry in a map. Keeps its value synchronised with
     * the map while it is not removed by other means, and then retains the
     * last value from the map before it was severed.
     *
     * @param owner     The map which owns this entry.
     * @param key       The key to the map's entry.
     */
    class Entry<K : Any, V> internal constructor(
            owner : WeakIdentityHashMap<K, V>,
            override val key : K
    ) : MutableMap.MutableEntry<K, V> {

        /** The map that owns this entry, until severed. */
        private var owner : WeakIdentityHashMap<K, V>? = owner

        override var value : V = owner.getValue(key).get()
            get() {
                // If we're not severed from the map, try get the latest value,
                // and if it's been removed from the map, sever the connection
                if (owner != null) {
                    val newValue = owner!!.getValue(key)
                    if (newValue is Absent)
                        owner = null
                    else
                        field = newValue.get()
                }
                return field
            }
            private set(value) {
                // Update the underlying map if not severed from it
                if (owner != null)
                    owner!![key] = value
                else
                    field = value
            }

        override fun setValue(newValue : V) : V {
            val current = value
            value = newValue
            return current
        }

    }

    /**
     * Iterator over the values in this map which keeps a strong reference
     * to the key of the next value so that it doesn't get garbage-collected
     * between calls to [hasNext] and [next]
     */
    inner class HoldingKeyIterator : MutableIterator<K> {
        private val intern = HoldingIterator()
        override fun hasNext() : Boolean = intern.hasNext()
        override fun next() : K = intern.next().second
        override fun remove() = intern.remove()
    }

    /**
     * Iterator over the values in this map which keeps a strong reference
     * to the key of the next value so that it doesn't get garbage-collected
     * between calls to [hasNext] and [next]
     */
    inner class HoldingValueIterator : MutableIterator<V> {
        private val intern = HoldingIterator()
        override fun hasNext() : Boolean = intern.hasNext()
        override fun next() : V = idToValue[intern.next().first] as V
        override fun remove() = intern.remove()
    }

    /**
     * Iterator over the values in this map which keeps a strong reference
     * to the key of the next value so that it doesn't get garbage-collected
     * between calls to [hasNext] and [next]
     */
    inner class HoldingEntryIterator : MutableIterator<MutableMap.MutableEntry<K, V>> {
        private val intern = HoldingIterator()
        override fun hasNext() : Boolean = intern.hasNext()
        override fun next() : MutableMap.MutableEntry<K, V> = Entry(this@WeakIdentityHashMap, intern.next().second)
        override fun remove() = intern.remove()
    }

    /**
     * Iterator over the entries in this map which creates a strong reference
     * to the next element in the iteration so that it doesn't get
     * garbage-collected between calls to [hasNext] and [next]. Also cleans
     * dead references from the map as it iterates.
     */
    inner class HoldingIterator : MutableIterator<Pair<Int, K>> {

        /** The strong reference to the next entry. */
        private var hold : Pair<Int, K>? = null

        /** Whether next has been called with the currently-held key. */
        private var nextCalled = false

        /** Internal iterator over the (possibly-dead) IDs of keys. */
        private val intern = idToRef.keys.iterator()

        override fun hasNext() : Boolean {
            // hasNext is idempotent between next calls
            if (!nextCalled && hold != null) return true

            // Reset the next flag
            nextCalled = false

            // Search the source iterator for the next live key
            while (intern.hasNext()) {
                // Get the next ID from the source iterator
                val id = intern.next()

                // Get the reference for the ID
                val ref = idToRef[id] as WeakReference<K>

                // If the key-reference is live, create the strong reference
                val strongRef = ref.get()
                if (strongRef != null) {
                    hold = Pair(id, strongRef)
                    return true
                } else {
                    removeIntern(id, ref)
                }
            }

            return false
        }

        override fun next() : Pair<Int, K> = ensureHasNext {
            return hold!!.also { nextCalled = true }
        }

        override fun remove() {
            // Can't call remove before next
            if (!nextCalled || hold == null) throw IllegalStateException()

            // Remove the held entry
            removeIntern(hold!!.first)

            // Release our hold on the strong-reference
            hold = null
        }

        /**
         * Removes the entry with the given ID from the map.
         *
         * @param id            The ID of the entry to remove.
         * @param passedRef     The reference to the key if already known.
         */
        private fun removeIntern(id : Int, passedRef : WeakReference<K>? = null) {
            // Get the reference to the identified entry if not passed
            val ref = passedRef ?: idToRef[id] as WeakReference<K>

            // Remove the ID using the iterator
            intern.remove()

            // Remove the associated information from the map
            refToID.remove(ref)
            idToValue.remove(id)
        }

    }
}
