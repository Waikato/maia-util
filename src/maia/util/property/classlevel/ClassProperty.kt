package maia.util.property.classlevel

/*
 * Defines the base class for class-level property delegates, which allows
 * accessing the property from either an instance or directly from the property
 * itself. Also defines methods for performing the class-level access.
 */

import maia.util.error.PreDelegationError
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Extension property which allows for access to a property from classes
 * instead of instances. Can be called from an instance, and automatically
 * translates to a class-level call for the instance's class.
 *
 * @param C
 *          The base-type of the classes/instances that can access this property.
 * @param R
 *          The type of the value of this property.
 */
abstract class ClassProperty<C : Any, R>: ReadWriteProperty<C, R> {

    /** Private access to the extension property this [ClassProperty] is delegated to. */
    private lateinit var propertyPrivate : KProperty<*>

    /** Private access to the extension property this [ClassProperty] is delegated to. */
    protected val property : KProperty<*>
        get() = ensureDelegated {
            propertyPrivate
        }

    /** Whether this property has been delegated to yet. */
    val isDelegated : Boolean
        get() = this::propertyPrivate.isInitialized

    /**
     * Ensures this ClassProperty has been delegated to before performing
     * the provided action.
     *
     * @param block
     *          The action to perform.
     * @return
     *          The result of the [block].
     * @param T
     *          The return-type of the [block].
     */
    inline fun <T> ensureDelegated(block : () -> T) : T {
        // Make sure this property has been delegated
        if (!isDelegated) throw PreDelegationError(this)

        return block()
    }

    // thisRef is typed Nothing? so that ClassProperty can only be delegated to
    // from top-level extensions
    operator fun provideDelegate(thisRef : Nothing?, property : KProperty<*>) : ClassProperty<C, R> {
        // ClassProperty can only be delegated once
        if (this::propertyPrivate.isInitialized) throw Exception("Reused class-level method property")

        // Remember which extension property we are delegated to
        this.propertyPrivate = property

        // Place this property in the registry for class-level access
        Registry[property] = this

        return this
    }

    final override fun getValue(
            thisRef : C,
            property : KProperty<*>
    ) : R {
        return getClassValue(thisRef::class)
    }

    /**
     * Gets the value of this property for the given class.
     *
     * @param cls
     *          The class accessing the value.
     * @return
     *          The value of the property.
     */
    abstract fun getClassValue(cls : KClass<out C>) : R

    final override fun setValue(
            thisRef : C,
            property : KProperty<*>,
            value : R
    ) {
        setClassValue(thisRef::class, value)
    }

    /**
     * Sets the value of this property for this class.
     *
     * @param cls
     *          The class assigning the value.
     * @param value
     *          The value being assigned to this property.
     */
    abstract fun setClassValue(cls : KClass<out C>, value : R)

    /**
     * Class providing access to this property with respect to
     * a bound class.
     *
     * @param cls
     *          The bound class to access this property from.
     */
    inner class Accessor(private val cls : KClass<out C>) {

        /**
         * Gets the value of the property for the bound class.
         *
         * @return
         *          The class-property's value for the class.
         */
        fun getValue() : R {
            return getClassValue(cls)
        }

        /**
         * Sets the value of the property for the bound class.
         *
         * @param value
         *          The value to set for the class.
         */
        fun setValue(value : R) {
            setClassValue(cls, value)
        }

    }

    /**
     * The registry of [ClassProperty]s. This allows access to the [ClassProperty]
     * instances from the extension properties themselves.
     */
    object Registry {

        /** Association from extension property to ClassProperty instance. */
        private val storage = HashMap<KProperty<*>, ClassProperty<*, *>>()

        internal operator fun contains(property : KProperty<*>) : Boolean {
            // Make an erroneous call to the property to ensure that it is initialised
            // TODO: Try to find a better way to do this
            if (property !in storage) try { property.call(null) } catch (e : Exception) {}

            return property in storage
        }

        internal operator fun get(property : KProperty<*>) : ClassProperty<*, *> {
            // Make sure the property is delegated
            if (property !in this) throw NoSuchElementException("$property is not a ClassProperty")

            return storage[property]!!
        }

        internal operator fun set(property : KProperty<*>, value : ClassProperty<*, *>) {
            storage[property] = value
        }

    }

}

/**
 * Gets an accessor to the class property represented by this reflection
 * property, bound to the given class.
 *
 * @param cls
 *          The class to use to access the property.
 * @return
 *          The accessor.
 * @param C
 *          The type of classes which can access this property.
 * @param R
 *          The value type of the property.
 */
fun <C : Any, R> KProperty1<C, R>.getClassPropertyAccessor(
        cls : KClass<out C>
) : ClassProperty<C, R>.Accessor {
    return (ClassProperty.Registry[this] as ClassProperty<C, R>).Accessor(cls)
}

/**
 * Gets an accessor to the class property represented by this reflection
 * property, bound to the reflection-property's class.
 *
 * @return
 *          The accessor.
 * @param C
 *          The type of classes which can access this property.
 * @param R
 *          The value type of the property.
 */
inline fun <reified C : Any, R> KProperty1<C, R>.getClassPropertyAccessor(
) : ClassProperty<C, R>.Accessor {
    return getClassPropertyAccessor(C::class)
}
