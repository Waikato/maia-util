package maia.util.datastructure

import java.util.*

/**
 * Implementation of [MutableSet] that only keeps a weak reference
 * to its values.
 *
 * @param cont The [WeakHashMap] that backs this set.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class WeakHashSet<T: Any> private constructor(
    cont: WeakHashMap<T, Unit>
): MutableSet<T> by cont.keys {

    /**
     * Constructs a new empty [WeakHashSet].
     */
    constructor(): this(WeakHashMap())

}
