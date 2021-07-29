package māia.util.property

/*
 * Defines the CachedReadOnlyProperty, which calculates a value for an object
 * when it is first accessed or whenever the cache is invalidated.
 */

import kotlin.reflect.KProperty

/**
 * Delegate read-only property which calculates a value on the first
 * time it is accessed, and then returns the cached value each time
 * thereafter. The cache can also be invalidated, in which case a new
 * value is calculated. Useful for properties which require some heavy
 * computation to calculate.
 *
 * @param cacheInitialiser
 *          The computation to perform to generate the property's value.
 * @param cacheInvalidator
 *          Optional operation to invalidate the cache. Takes the current
 *          value of the cache and returns whether the cache is invalid.
 * @param perObject
 *          Whether the value should be calculated for each individual object,
 *          or if equal objects should be treated as having equal values for
 *          this property.
 * @param R
 *          The type of object that holds this property.
 * @param T
 *          The type of the property's value.
 */
class CachedReadOnlyProperty<R : Any, T>(
        private val cacheInitialiser : R.() -> T,
        private val cacheInvalidator : (R.(T) -> Boolean)? = null,
        perObject : Boolean = true
) : ReadOnlyPropertyWithMemory<R, T, T>(perObject) {

    override fun getValue(thisRef : R, property : KProperty<*>) : T {
        // Calculate or get the cached value
        var value : T
        if (thisRef !in memory) {
            // Calculate and cache a value if it's not already cached
            value = thisRef.cacheInitialiser()
            memory[thisRef] = value
        } else {
            // Otherwise get the cached value, and recalculate/re-cache if it's
            // invalid
            value = memory[thisRef]
            if (cacheInvalidator?.let { thisRef.it(value) } == true) {
                value = thisRef.cacheInitialiser()
                memory[thisRef] = value
            }
        }

        return value
    }

}
