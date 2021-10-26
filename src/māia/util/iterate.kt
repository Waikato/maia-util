package māia.util

/**
 * Package for working with iterables/iterators.
 */

/**
 * Adds a 'map' method to iterators which applies some mapping
 * function to each element of the iterator.
 *
 * @receiver    The source iterator.
 * @param func  The function to map source elements to output elements.
 * @return      An iterator over the mapped elements.
 */
fun <I, O> Iterator<I>.map(func : (I) -> O) : Iterator<O> {
    return object : Iterator<O> {
        override fun hasNext(): Boolean = this@map.hasNext()
        override fun next(): O = func(this@map.next())
    }
}

inline fun <I, reified O> Collection<I>.arrayMap(func : (I) -> O) : Array<O> {
    val i = iterator()
    return Array(size) {
        func(i.next())
    }
}

/**
 * Adds the [Iterable.joinTo] method to iterators.
 *
 * @receiver    The source iterator to join.
 */
fun <T, A : Appendable> Iterator<T>.joinTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    return asIterable().joinTo(buffer, separator, prefix, postfix, limit, truncated, transform)
}

/**
 * Adds the [Iterable.joinToString] method to iterators.
 *
 * @receiver    The source iterator to join.
 */
fun <T> Iterator<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    return asIterable().joinToString(separator, prefix, postfix, limit, truncated, transform)
}

/**
 * Joins this iterator to a string, returning null when the iterator is empty.
 *
 * @receiver    The source iterator to join.
 * @return      The joined string, or null if the iterator is empty.
 */
fun <T> Iterator<T>.joinToStringOrNull(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((T) -> CharSequence)? = null
): String? {
    return if (hasNext())
        joinToString(separator, prefix, postfix, limit, truncated, transform)
    else
        null
}

/**
 * Utility function to create an iterator over an explicit dataset of items.
 *
 * @param items     The items to yield from the iterator.
 * @return          An iterator which yields the given items.
 */
fun <T> itemIterator(vararg items : T) : Iterator<T> = items.iterator()

/**
 * Utility extension method to create an iterator over a single individual object.
 *
 * @receiver    The object to return from the iterator.
 * @return      An iterator that returns the receiver.
 */
fun <T> T.iterateThis() : Iterator<T> = itemIterator(this)

/**
 * An iterator that iterates a given number of times by calling a source function
 * with the iteration number as an argument.
 *
 * @param times     The number of times to iterate.
 * @param source    The source function to call for each element.
 * @param T         The type of element of the iterator.
 */
class CountingIterator<T>(val times : Long, val source : (Long) -> T) : Iterator<T> {
    /** The number of times iterated so far. */
    private var count = 0L
    override fun hasNext(): Boolean = times < 0 || count < times
    override fun next(): T = ensureHasNext { source(count++) }
}

/**
 * Performs advanced processing of an iterator, optionally converting/
 * removing/inserting elements during the iteration.
 *
 * @param block
 *          The block which returns the elements to iterate over for a
 *          source element, or null to yield no elements.
 * @return
 *          An iterator over the processed items.
 */
fun <T, R> Iterator<T>.process(block: (T) -> Iterator<R>?): Iterator<R> {
    return object : Iterator<R> {

        var buffer: Iterator<R> = EmptyIterator

        override fun hasNext(): Boolean {
            loadBuffer()
            return buffer.hasNext()
        }

        override fun next(): R = ensureHasNext {
            return buffer.next()
        }

        private fun loadBuffer() {
            while (!buffer.hasNext() && this@process.hasNext()) {
                val item = this@process.next()

                buffer = block(item) ?: continue
            }
        }

    }
}

/**
 * Filters an iterator for items matching a predicate.
 *
 * @param predicate     The predicate which items must match to be returned.
 * @return              An iterator over the filtered items.
 */
fun <T> Iterator<T>.filter(predicate : (T) -> Boolean) : Iterator<T> {
    return process { if (predicate(it)) itemIterator(it) else null }
}

/**
 * Collects the elements of this iterator into a collection.
 *
 * @param collection    The collection to contain the items.
 * @return              The collection with the items added.
 */
fun <T, C : MutableCollection<in T>> Iterator<T>.collect(collection: C) : C {
    forEach { collection.add(it) }
    return collection
}

/**
 * Collects the elements of this iterator into a map.
 *
 * @receiver        The iterator of items to add to the map.
 * @param map       The map to add the items to.
 * @param keyFunc   A function to determine the key from the item.
 * @return          The map with the items added.
 * @param T         The type of value in the iterator/map.
 * @param K         The type of keys to the map.
 * @param M         The type of map.
 */
fun <T, K, M : MutableMap<K, in T>> Iterator<T>.collect(map : M, keyFunc : (T) -> K) : M {
    forEach { map[keyFunc(it)] = it }
    return map
}

/**
 * Collects the elements of an iterator into an array.
 *
 * @receiver    The iterator to collect.
 * @return      An array containing the elements from the iterator.
 */
inline fun <reified T> Iterator<T>.toArray() : Array<T> = collect(ArrayList()).toTypedArray()

/**
 * Creates an iterable which returns the receiver as its iterator.
 *
 * @receiver    The source iterator.
 * @return      An iterable which returns the receiver iterator.
 */
fun <T> Iterator<T>.asIterable() : Iterable<T> = Iterable { this@asIterable }

// region Zip

/**
 * Returns an iterator over pair of elements from the source iterators
 * until either iterator runs out.
 *
 * @param i1    The first iterator.
 * @param i2    The second iterator.
 * @return      An iterator over pairs of values from the two sources.
 * @param T1    The type of element in the first iterator.
 * @param T2    The type of element in the second iterator.
 */
fun <T1, T2> zip(i1 : Iterator<T1>, i2 : Iterator<T2>) : Iterator<Pair<T1, T2>> {
    return object : Iterator<Pair<T1, T2>> {
        override fun hasNext() : Boolean = i1.hasNext() && i2.hasNext()
        override fun next() : Pair<T1, T2> = Pair(i1.next(), i2.next())
    }
}

/**
 * Returns an iterator over pair of elements from the source iterables
 * until either iterator runs out.
 *
 * @param i1    The first iterable.
 * @param i2    The second iterable.
 * @return      An iterator over pairs of values from the two sources.
 * @param T1    The type of element in the first iterable.
 * @param T2    The type of element in the second iterable.
 */
fun <T1, T2> zip(i1 : Iterable<T1>, i2 : Iterable<T2>) : Iterator<Pair<T1, T2>> {
    return zip(i1.iterator(), i2.iterator())
}

// endregion

fun <T> Iterator<T>.all(predicate : (T) -> Boolean) : Boolean {
    forEach {
        if (!predicate(it)) return false
    }

    return true
}

fun <T> Iterator<T>.any(predicate : (T) -> Boolean) : Boolean {
    forEach {
        if (predicate(it)) return true
    }

    return false
}

fun indexIterator(from : Int, to : Int) : Iterator<Int> {
    return (from until to).iterator()
}

fun indexIterator(to : Int) : Iterator<Int> {
    return indexIterator(0, to)
}

fun <T> Iterator<T>.tally() : HashMap<T, Int> {
    val tally = HashMap<T, Int>()
    forEach {
        val currentSum = tally[it] ?: 0
        tally[it] = currentSum + 1
    }
    return tally
}

fun <T> duplicates(iterator : Iterator<T>) : HashSet<T> {
    return iterator.tally()
            .iterator()
            .filter { it.value > 1 }
            .map { it.key }
            .collect(HashSet())
}

object StopIteration : Throwable()

class Generator<T>(
        private var source : ((Int) -> T)?
) : Iterator<T> {

    private var value : T? = null

    private var valid = false

    private var index = 0

    override fun hasNext() : Boolean {
        populate()
        return valid
    }

    override fun next() : T = ensureHasNext {
        valid = false
        val returnValue = value as T
        value = null
        return returnValue
    }

    private fun populate() {
        if (!valid && source != null) {
            try {
                value = source!!(index++)
                valid = true
            } catch (e : StopIteration) {
                valid = false
                source = null
            }
        }
    }

}

/**
 * Allows an [Array] to meet the [Iterable] interface.
 *
 * @receiver    The array to view as iterable.
 * @return      An object implementing the [Iterable] interface,
 *              which returns the array iterator.
 */
fun <T> Array<T>.asIterable() : Iterable<T> = Iterable { iterator() }

fun <T> Iterator<Pair<T, T>>.allEqual() = all {
    it.first == it.second
}

/**
 * TODO
 */
fun <E> iterableEquivalency(
    elementEquivalency : Equivalency<E> = EQUALITY_EQUIVALENCY
): Equivalency<Iterable<E>> {
    return Equivalency { a, b ->
        iterablesAreEquivalent(a, b, elementEquivalency::test)
    }
}

/**
 * TODO
 */
inline fun <E> iterablesAreEquivalent(
    a: Iterable<E>,
    b: Iterable<E>,
    crossinline elementsAreEquivalent: (a: E, b: E) -> Boolean
): Boolean {
    val aIter = a.iterator()
    val bIter = b.iterator()
    if (aIter === bIter) return true
    return aIter.all { aElem ->
        bIter.hasNext() && elementsAreEquivalent(aElem, bIter.next())
    } && !bIter.hasNext()
}

/**
 * Utility extension function for iterators which throws if
 * the iterator does not have a next element, otherwise performing
 * the provide block of code.
 *
 * @receiver
 *          The source iterator.
 * @param block
 *          The code to run if the iterator has a next element.
 * @return
 *          The result of the [block].
 * @throws NoSuchElementException
 *          If the iterator has no next element.
 * @param E
 *          The type of the elements of the iterator.
 * @param R
 *          The return-type of the [block].
 */
inline fun <E, R> Iterator<E>.ensureHasNext(block : Iterator<E>.() -> R) : R {
    if (!hasNext()) throw NoSuchElementException()
    return block()
}

/**
 * Utility extension function for list iterators which throws if
 * the iterator does not have a previous element, otherwise performing
 * the provide block of code.
 *
 * @receiver
 *          The source iterator.
 * @param block
 *          The code to run if the iterator has a previous element.
 * @return
 *          The result of the [block].
 * @throws NoSuchElementException
 *          If the iterator has no previous element.
 * @param T
 *          The type of the elements of the iterator.
 * @param R
 *          The return-type of the [block].
 */
inline fun <T, R> ListIterator<T>.ensureHasPrevious(block : ListIterator<T>.() -> R) : R {
    if (!hasPrevious()) throw NoSuchElementException()
    return block()
}

// region Chain

/**
 * Iterator which chains together several sub-iterators in the
 * order they are given.
 *
 * @param components    The sub-iterators to chain.
 * @param T             The type of element in the sub-iterators.
 */
class IteratorChain<T>(private val components : Iterator<Iterator<T>>) : Iterator<T> {

    /**
     * Constructor for explicitly-defined sub-iterators.
     *
     * @param components    The sub-iterators to chain.
     */
    constructor(vararg components : Iterator<T>) : this(components.iterator())

    /** The next sub-iterator with available items. */
    private lateinit var nextComponent : Iterator<T>

    override fun hasNext() : Boolean {
        fastForward()
        return ::nextComponent.isInitialized && nextComponent.hasNext()
    }

    override fun next() : T = ensureHasNext {
        return nextComponent.next()
    }

    /**
     * Moves the [nextComponent] to the next sub-iterator that has
     * elements to yield.
     */
    private fun fastForward() {
        if (!::nextComponent.isInitialized) {
            if (!components.hasNext())
                return
            else
                nextComponent = components.next()
        }

        while (components.hasNext() && !nextComponent.hasNext()) {
            nextComponent = components.next()
        }
    }

}

/**
 * Creates an iterator which chains together the given sub-iterators.
 *
 * @param iterators     The sub-iterators.
 * @param T             The element type of the sub-iterators.
 * @return              An iterator over the sub-iterators, in given order.
 */
fun <T> chain(vararg iterators : Iterator<T>) : Iterator<T> {
    return IteratorChain(*iterators)
}

/**
 * Takes each element in the source iterator, maps it to an iterator
 * of output elements, and returns an iterator over the chained concatenation
 * of the mapped iterators.
 *
 * @receiver    The source iterator.
 * @param func  The function mapping source elements to output iterators.
 * @return      An iterator over the chained elements of the mapped iterators.
 * @param I     The type of the input elements.
 * @param O     The type of the output elements.
 */
fun <I, O> Iterator<I>.chainMap(func : (I) -> Iterator<O>) : Iterator<O> {
    return IteratorChain(map(func))
}

// endregion

// region Enumerate

/**
 * An iterator over pairs of (index, value) which enumerates
 * another iterator.
 *
 * @param source    The iterator to enumerate.
 * @param start     The starting index to enumerate from.
 * @param T         The type of value being iterated.
 */
class EnumeratingIterator<T>(
        private val source : Iterator<T>,
        start : Int = 0
) : Iterator<Pair<Int, T>> {

    /** The index of the next element, starting at the specified value. */
    private var index = start

    // If the source iterator has another element, so do we
    override fun hasNext(): Boolean = source.hasNext()

    // The next element is the pair of the index and the source element
    override fun next(): Pair<Int, T> = Pair(index++, source.next())
}

/**
 * Takes an iterator and converts it to an iterator over
 * (index, element) pairs.
 *
 * @receiver        The iterator to enumerate.
 * @param start     The starting value of the indices (default is 0).
 * @return          An iterator over (index, element) pairs.
 */
fun <T> Iterator<T>.enumerate(start : Int = 0) : Iterator<Pair<Int, T>> {
    return EnumeratingIterator(this, start)
}

/**
 * Takes an iterable and converts it to an iterator over
 * (index, element) pairs.
 *
 * @receiver        The iterable to enumerate.
 * @param start     The starting value of the indices (default is 0).
 * @return          An iterator over (index, element) pairs.
 */
fun <T> Iterable<T>.enumerate(start : Int = 0) : Iterator<Pair<Int, T>> {
    return iterator().enumerate(start)
}

/**
 * Takes an array and converts it to an iterator over
 * (index, element) pairs.
 *
 * @receiver        The array to enumerate.
 * @param start     The starting value of the indices (default is 0).
 * @return          An iterator over (index, element) pairs.
 */
fun <T> Array<T>.enumerate(start : Int = 0) : Iterator<Pair<Int, T>> {
    return iterator().enumerate(start)
}

// endregion

/**
 * Finds the maximum element in an iterator of comparable items.
 *
 * @receiver    The iterator to search.
 * @return      A pair of (index, element) for the maximum element.
 * @param E     The type of comparable element in the iterator.
 */
fun <E : Comparable<E>> Iterator<E>.maxWithIndex() : Pair<Int, E> {
    var maxIndex = 0
    val maxValue = asIterable().reduceIndexed { index, current, next ->
        if (next > current) {
            maxIndex = index
            next
        } else {
            current
        }
    }
    return Pair(maxIndex, maxValue)
}

/**
 * Finds the maximum value of an iterator of [Comparable]s.
 */
fun <E : Comparable<E>> Iterator<E>.max() : E {
    return asIterable().reduce(maxReducer<E>()::reduce)
}


/**
 * Performs an action for each element of the iteration, without
 * changing iterated elements.
 *
 * @receiver
 *          The iterator of elements on which to perform the action.
 * @param block
 *          The action to perform on each element.
 * @return
 *          An iterator over the receiver's elements which performs the
 *          action inline.
 * @param E
 *          The type of element in the iterator.
 */
fun <E> Iterator<E>.inline(block : (E) -> Unit) : Iterator<E> {
    return map {
        block(it)
        it
    }
}

class RandomAccessListIterator<T>(
    private val list: List<T>,
    startIndex: Int
): ListIterator<T> {
    init {
        ensureIndexInRange(startIndex, list.size, true) {}
    }
    private var cursor = startIndex
    override fun hasNext() : Boolean = cursor < list.size
    override fun hasPrevious() : Boolean = cursor > 0
    override fun next() : T = ensureHasNext { list[cursor++] }
    override fun nextIndex() : Int = cursor
    override fun previous() : T = ensureHasPrevious { list[--cursor] }
    override fun previousIndex() : Int = cursor - 1
}

class ElementIterator<E>(
    private val get: (Int) -> E,
    private val size: Int
): Iterator<E> {
    private var index = 0;
    override fun hasNext() : Boolean = index < size
    override fun next() : E = get(index++)
}

/**
 * Finds the first duplicate item in an iterable, with the option
 * to use proxy objects for comparison.
 *
 * @receiver
 *          The iterable to search.
 * @param proxyFunc
 *          An optional mapping to proxy objects for comparison.
 * @return
 *          The first duplicate found, or Absent if no duplicates found.
 */
inline fun <T> Iterable<T>.findFirstDuplicate(
    proxyFunc: (T) -> Any? = { it }
): Optional<T> {
    val seen = HashSet<Any?>()
    for (item in this) {
        val proxy = proxyFunc(item)
        if (proxy in seen) return item.asOptional
        seen.add(proxy)
    }

    return Absent
}
