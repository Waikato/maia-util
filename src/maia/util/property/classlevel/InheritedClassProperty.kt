package maia.util.property.classlevel

/*
 * Defines the InheritedClassProperty, which provides automatic deferral to
 * super-class values for a class-level property.
 */

import maia.util.superClassesUpTo
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance

/**
 * Class-level property which automatically returns a value for a super-class
 * if no value exists for the class accessing the property. The property stores
 * a map of overrides for each class for which one is defined, but in an internal
 * type [M], which can be translated by overriding classes to the property's
 * value-type.
 *
 * @param baseCls
 *          The base-class of this property.
 * @param base
 *          The internal value of this property for the base-class.
 * @param C
 *          The base-type of the classes/instances that can access this property.
 * @param R
 *          The type of the value of this property.
 * @param M
 *          The internal memory-type of the property.
 */
abstract class InheritedClassProperty<C : Any, R, M>(
        val baseCls : KClass<C>,
        protected val base : M
) : ClassProperty<C, R>() {

    /** The override memory for sub-classes. */
    private val overrides = HashMap<KClass<out C>, M>()

    final override fun getClassValue(cls : KClass<out C>) : R {
        val (overrideCls, overrideValue) = getOverride(cls)
        return getClassValueFromOverride(cls, overrideCls, overrideValue)
    }

    /**
     * Get the value for the given accessing class, given the most
     * closely-defined override class and its internal value.
     *
     * @param accessingCls
     *          The class accessing this property.
     * @param overrideCls
     *          The closest super-class which defines an override for this
     *          property.
     * @param overrideValue
     *          The internal memory-value for [overrideCls].
     */
    abstract fun getClassValueFromOverride(
            accessingCls : KClass<out C>,
            overrideCls : KClass<out C>,
            overrideValue : M
    ) : R

    final override fun setClassValue(cls : KClass<out C>, value : R) {
        val (overrideCls, overrideValue) = getOverride(cls)
        overrides[cls] = setOverrideForClassValue(
                cls,
                value,
                overrideCls,
                overrideValue
        )
    }

    /**
     * Defines an override for the value of this property for the given class,
     * given the (current) most closely-defined override class and its internal
     * value.
     *
     * @param cls
     *          The class accessing this property.
     * @param value
     *          The override value being defined for the [cls].
     * @param currentCls
     *          The (current) closest super-class which defines an override for
     *          this property.
     * @param currentValue
     *          The (current) internal memory-value for [currentCls].
     */
    abstract fun setOverrideForClassValue(
            cls : KClass<out C>,
            value : R,
            currentCls : KClass<out C>,
            currentValue : M
    ) : M

    /**
     * Searches the [overrides] definitions for the closest class to the given
     * class which defines an override for this property.
     *
     * @param cls
     *          The class to find an override for.
     * @return
     *          The class and value of the nearest defined override.
     */
    private fun getOverride(cls : KClass<out C>) : Pair<KClass<out C>, M> {
        var overrideCls = cls

        findExplicitOverride@while (true) {
            // Ensure the class is statically initialised
            overrideCls.companionObjectInstance

            // If an override exists for this class, return it
            @Suppress("UNCHECKED_CAST")
            if (overrideCls in overrides) return Pair(overrideCls, overrides[overrideCls] as M)

            // Get the super-classes to this one on the way to baseClass
            val superClasses = overrideCls.superClassesUpTo(baseCls)

            overrideCls = when (superClasses.size) {
                0 -> break@findExplicitOverride
                1 -> superClasses[0] as KClass<out C>
                else -> throw Exception("Diamond inheritance; requires override")
            }
        }

        return Pair(baseCls, base)
    }

}

/**
 * Overrides the value of a class-level property for a given class.
 *
 * @receiver
 *          The reflection property identifying the class-property to override.
 * @param value
 *          The value to give the property for the class.
 * @param C
 *          The base-type of the classes/instances that can access this property.
 * @param R
 *          The type of the value of this property.
 */
inline fun <reified C : Any, R> KProperty1<C, R>.override(
        value : R
) {
    getClassPropertyAccessor(C::class).setValue(value)
}
