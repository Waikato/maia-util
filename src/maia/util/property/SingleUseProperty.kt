package maia.util.property

/*
 * Defines the SingleUseReadOnlyProperty and SingleUseReadWriteProperty,
 * which are base classes for properties which can only be used by a single
 * owning object.
 */

import maia.util.error.PreDelegationError
import maia.util.error.RedelegationError
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Base class for read-only properties which can only be owned by a single
 * object.
 *
 * @param R
 *          The type of object which owns the delegated property.
 * @param T
 *          The type of the property value.
 */
abstract class SingleUseReadOnlyProperty<R : Any, out T>
    :   ReadOnlyProperty<R, T>,
        PropertyDelegateProvider<R, SingleUseReadOnlyProperty<R, T>>
{

    /** The object which owns the property. */
    lateinit var owner : R
        private set

    /** The reflection property for this property. */
    lateinit var property : KProperty<*>
        private set

    /** The name that this property has been delegated to. */
    open val name : String
        get() = ensureDelegated {
            property.name
        }

    /** Whether this property has been delegated to yet. */
    val isDelegated : Boolean
        get() = ::owner.isInitialized

    final override operator fun provideDelegate(thisRef : R, property : KProperty<*>) : SingleUseReadOnlyProperty<R, T> {
        // Make sure we are only delegated to once
        if (isDelegated) throw RedelegationError(this::class)

        // Call custom delegation functionality
        onDelegation(thisRef, property, property.name)

        // Remember our owner/property
        this.owner = thisRef
        this.property = property

        return this
    }

    /**
     * Performs custom property operations on delegation. The [owner]/[property]/[name]
     * members are not set before this is called, so are passed as arguments. [isDelegated]
     * returns false.
     */
    protected abstract fun onDelegation(owner : R, property : KProperty<*>, name: String)

    /**
     * Performs the provided function under the assurance that
     * this property has been delegated to by an object.
     *
     * @param block
     *          The function to perform.
     * @return
     *          The result of the function.
     * @param Re
     *          The return-type of the function.
     * @throws PreDelegationError
     *          If this property hasn't been delegated to.
     */
    inline fun <Re> ensureDelegated(block : () -> Re) : Re {
        // Ensure we have been delegated to
        if (!isDelegated) throw PreDelegationError(this::class)

        // Return the result of the inner block
        return block()
    }

    /**
     * Performs the provided function under the assurance that
     * the object/reflection-property accessing this property are
     * the same ones registered during delegation.
     *
     * @param thisRef
     *          The object accessing this property.
     * @param property
     *          The reflection-property being used to access this property.
     * @param block
     *          The function to perform.
     * @return
     *          The result of the function.
     * @param Re
     *          The return-type of the function.
     * @throws PreDelegationError
     *          If this property hasn't been delegated to.
     * @throws RedelegationError
     *          If the accessors aren't the ones provided during delegation.
     */
    inline fun <E> ensureAccessor(
            thisRef : R,
            property : KProperty<*>,
            block : () -> E
    ) : E = ensureDelegated {
        // Ensure we are being accessed by our owner only
        if (thisRef !== owner || property !== this.property)
            throw RedelegationError(this::class)

        // Return the result of the inner block
        return block()
    }

    final override operator fun getValue(
            thisRef: R,
            property: KProperty<*>
    ) : T = ensureAccessor(thisRef, property) {
        return getValue()
    }

    /**
     * Gets the value of this property.
     *
     * @return
     *          The property's value.
     */
    protected abstract fun getValue() : T
}

/**
 * Base class for read-write properties which can only be owned by a single
 * object.
 *
 * @param R the type of object which owns the delegated property.
 * @param T the type of the property value.
 */
abstract class SingleUseReadWriteProperty<R : Any, T>
    :   ReadWriteProperty<R, T>,
        PropertyDelegateProvider<R, SingleUseReadWriteProperty<R, T>>
{

    /** The object which owns the property. */
    lateinit var owner : R
        private set

    /** The reflection property for this property. */
    lateinit var property : KProperty<*>
        private set

    /** The name that this property has been delegated to. */
    open val name : String
        get() = ensureDelegated {
            property.name
        }

    /** Whether this property has been delegated to yet. */
    val isDelegated : Boolean
        get() = ::owner.isInitialized

    final override operator fun provideDelegate(thisRef : R, property : KProperty<*>) : SingleUseReadWriteProperty<R, T> {
        // Make sure we are only delegated to once
        if (isDelegated) throw RedelegationError(this::class)

        // Call custom delegation functionality
        onDelegation(thisRef, property, property.name)

        // Remember our owner/property
        this.owner = thisRef
        this.property = property

        return this
    }

    /**
     * Performs custom property operations on delegation. The [owner]/[property]/[name]
     * members are not set before this is called, so are passed as arguments. [isDelegated]
     * returns false.
     */
    protected abstract fun onDelegation(owner : R, property : KProperty<*>, name: String)

    /**
     * Performs the provided function under the assurance that
     * this property has been delegated to by an object.
     *
     * @param block
     *          The function to perform.
     * @return
     *          The result of the function.
     * @param Re
     *          The return-type of the function.
     * @throws PreDelegationError
     *          If this property hasn't been delegated to.
     */
    inline fun <E> ensureDelegated(block : () -> E) : E {
        // Ensure we have been delegated to
        if (!isDelegated) throw PreDelegationError(this::class)

        // Return the result of the inner block
        return block()
    }

    /**
     * Performs the provided function under the assurance that
     * the object/reflection-property accessing this property are
     * the same ones registered during delegation.
     *
     * @param thisRef
     *          The object accessing this property.
     * @param property
     *          The reflection-property being used to access this property.
     * @param block
     *          The function to perform.
     * @return
     *          The result of the function.
     * @param Re
     *          The return-type of the function.
     * @throws PreDelegationError
     *          If this property hasn't been delegated to.
     * @throws RedelegationError
     *          If the accessors aren't the ones provided during delegation.
     */
    inline fun <Re> ensureAccessor(
            thisRef : R,
            property : KProperty<*>,
            block : () -> Re
    ) : Re = ensureDelegated {
        // Ensure we are being accessed by our owner only
        if (thisRef !== owner || property !== this.property)
            throw RedelegationError(this::class)

        // Return the result of the inner block
        return block()
    }

    final override operator fun getValue(
            thisRef: R,
            property: KProperty<*>
    ) : T = ensureAccessor(thisRef, property) {
        return getValue()
    }

    /**
     * Gets the value of this property.
     *
     * @return
     *          The property's value.
     */
    abstract fun getValue() : T

    final override operator fun setValue(
            thisRef: R,
            property: KProperty<*>,
            value: T
    ) = ensureAccessor(thisRef, property) {
        setValue(value)
    }

    /**
     * Sets the value of this property.
     *
     * @param value
     *          The property's new value.
     */
    abstract fun setValue(value : T)

}
