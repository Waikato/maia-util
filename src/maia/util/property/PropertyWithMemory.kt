package maia.util.property

import maia.util.datastructure.WeakIdentityHashMap
import java.util.*
import kotlin.NoSuchElementException
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty

/**
 * Class which provides memory for objects inside a property with memory.
 *
 * @param perObject
 *          Whether equal objects should not share memory.
 * @param R
 *          The type of object which owns the memory.
 * @param M
 *          The type of memory held for the objects.
 */
class PropertyMemory<in R : Any, M>(perObject : Boolean) {

    /** A weakly-referenced map from object to the memory for the object. */
    private val memory : MutableMap<R, M> = if (perObject) WeakIdentityHashMap() else WeakHashMap()

    operator fun contains(accessor : R) : Boolean {
        return accessor in memory
    }

    operator fun get(accessor : R) : M {
        // Make sure the accessor has memory allocated
        if (accessor !in memory) throw NoSuchElementException("$accessor has no allocated memory")

        @Suppress("UNCHECKED_CAST")
        return memory[accessor] as M
    }

    operator fun set(accessor : R, value : M) {
        memory[accessor] = value
    }

    operator fun minusAssign(accessor : R) {
        memory.remove(accessor)
    }

}

/**
 * Base class for read-only properties which require memory for their accessors.
 *
 * @param perObject
 *          Whether equal objects should not share memory.
 * @param R
 *          The type of object which owns the delegated property.
 * @param T
 *          The type of the property value.
 * @param M
 *          The type of memory held for the objects.
 */
abstract class ReadOnlyPropertyWithMemory<in R : Any, out T, M>(
        perObject : Boolean
) : ReadOnlyProperty<R, T> {

    /** The memory for the accessors. */
    protected val memory = PropertyMemory<R, M>(perObject)

}

/**
 * Base class for read-write properties which require memory for their accessors.
 *
 * @param perObject
 *          Whether equal objects should not share memory.
 * @param R
 *          The type of object which owns the delegated property.
 * @param T
 *          The type of the property value.
 * @param M
 *          The type of memory held for the objects.
 */
abstract class ReadWritePropertyWithMemory<in R : Any, T, M>(
        perObject : Boolean
) : ReadWriteProperty<R, T> {

    /** The memory for the accessors. */
    protected val memory = PropertyMemory<R, M>(perObject)

}
