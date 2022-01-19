package maia.util.property.classlevel.method

/*
 * Defines the ClassMethodProperty, which allows for methods to be set at a
 * class-level and called from instances/classes alike.
 *
 * TODO: Work-in-progress.


import maia.util.property.classlevel.InheritedClassProperty
import maia.util.property.classlevel.getClassPropertyAccessor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1

/**
 * Property which handles defining/overriding class-level methods.
 *
 * @param base
 *          The definition of the method on the base-class.
 * @param C
 *          The base-type of the classes/instances that can access this property.
 * @param R
 *          The return-type of the methods of this property.
 * @param M
 *          The type of class-level method handled by this property.
 */
class ClassMethodProperty<C : Any, R, M : ClassMethod<out C, out C, C, R, *>> internal constructor(
        base : M
) : InheritedClassProperty<C, M, M>(base.definedBy as KClass<C>, base) {

    init {
        // Take ownership of the base method definition
        base.takeOwnership(this)

        val x = KFunction
    }

    override fun getClassValueFromOverride(
            accessingCls : KClass<out C>,
            overrideCls : KClass<out C>,
            overrideValue : M) : M {
        return overrideValue.rebind(accessingCls)
    }

    override fun setOverrideForClassValue(
            cls : KClass<out C>,
            value : M,
            currentCls : KClass<out C>,
            currentValue : M
    ) : M {
        // Can't redefine methods
        if (cls == currentCls) throw Exception("Redefined class-level method $property for class $cls")

        // Take ownership of the overriding definition
        value.takeOwnership(this)

        return value
    }

}

// region Non-Reified Constructors

/**
 * TODO
 */
fun <C : Any, R> classMethodProperty(
        baseCls : KClass<C>,
        body: KClass<out C>.() -> R
) : ClassMethodProperty<C, R, ClassMethod0<C, R>> {
    return ClassMethodProperty(classMethod(baseCls, body))
}

/**
 * Creates a class-method property which takes one parameter
 */
fun <C : Any, P1, R> classMethodProperty(
        baseCls : KClass<C>,
        body: KClass<out C>.(P1) -> R
) : ClassMethodProperty<C, R, ClassMethod1<C, P1, R>> {
    return ClassMethodProperty(classMethod(baseCls, body))
}

// endregion

// region Reified Constructors

/**
 * TODO
 */
inline fun <reified C : Any, R> classMethodProperty(
        noinline body: KClass<out C>.() -> R
) : ClassMethodProperty<C, R, ClassMethod0<C, R>> {
    return classMethodProperty(C::class, body)
}

/**
 * TODO
 */
inline fun <reified C : Any, P1, R> classMethodProperty(
        noinline body: KClass<out C>.(P1) -> R
) : ClassMethodProperty<C, R, ClassMethod1<C, P1, R>> {
    return classMethodProperty(C::class, body)
}

// endregion

// region Invocation

/**
 * TODO
 */
inline operator fun <reified D : C, C : Any, R> KProperty1<D, ClassMethod0<C, R>>.invoke() : R {
    return getClassPropertyAccessor().getValue().invoke()
}

/**
 * TODO
 */
inline operator fun <reified D : C, C : Any, P1, R> KProperty1<D, ClassMethod1<C, P1, R>>.invoke(p1 : P1) : R {
    return getClassPropertyAccessor().getValue().invoke(p1)
}

// endregion

// region Override

/**
 * TODO
 */
inline fun <reified D : C, C : Any, R> KProperty1<D, ClassMethod0<C, R>>.override(
        noinline body: ClassMethod<C, R, ClassMethod0<C, R>>.ClassWithSuper<out D>.() -> R
) {
    getClassPropertyAccessor(D::class).setValue(classMethod(body))
}

/**
 * TODO
 */
inline fun <reified D : C, C : Any, P1, R> KProperty1<D, ClassMethod1<C, P1, R>>.override(
        noinline body: ClassMethod<C, R, ClassMethod1<C, P1, R>>.ClassWithSuper<out D>.(P1) -> R
) {
    getClassPropertyAccessor(D::class).setValue(classMethod(body))
}

// endregion
*/
