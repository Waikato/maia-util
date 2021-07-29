package māia.util.property.classlevel.method

/*
 * TODO: Work-in-progress.
 *

import māia.util.superClass
import kotlin.reflect.KClass

/**
 * TODO
 */
class ClassWithSuper<R, F : Function<R>, T : Any>(
        internal val cls : KClass<T>,
        private val owner : ClassMethod<T, *, *, R, F>
) : KClass<T> by cls {

    val supr : ClassMethod<*, *, *, R, F>
        get() {
            if (owner.definedBy == owner.owner!!.baseCls) throw Exception("No super")
            return owner.owner!!.getClassValue(owner.definedBy.superClass as KClass<out C>).rebind(owner.boundTo.cls)
        }

    override fun toString() : String {
        return "$cls with super up to ${owner.owner!!.baseCls}"
    }

}

/**
 * TODO
 */
abstract class ClassMethod<B : D, D : C, C : Any, R, F : Function<R>>(
        internal val definedBy : KClass<D>,
        boundTo : KClass<B>
) {

    /** TODO */
    internal var owner : ClassMethodProperty<C, R, *>? = null
        private set

    /** TODO */
    internal val boundTo = ClassWithSuper(boundTo, this)

    /**
     * TODO
     */
    internal abstract fun <B2 : D> rebind(
            cls : KClass<B2>
    ) : ClassMethod<B2, D, C, R, F>

    /**
     * TODO
     */
    internal fun takeOwnership(owner : ClassMethodProperty<>) {
        if (this.owner != null) throw Exception("Already owned")
        this.owner = owner
    }

    abstract fun getFBound(cls : ClassWithSuper<B>) : F

}

/**
 * TODO
 */
class ClassMethod0<B : D, D : C, C : Any, R>(
        definedBy : KClass<D>,
        boundTo : KClass<B>,
        private val body : ClassWithSuper<out C>.() -> R
) : ClassMethod<B, D, C, R, () -> R>(definedBy, boundTo) {

    override fun <B2 : D> rebind(cls : KClass<B2>) : ClassMethod<B2, D, C, R, () -> R> {
        return ClassMethod0(definedBy, cls, body)
    }

    override fun getFBound(cls : ClassWithSuper<B>) : () -> R {
        return { cls.body() }
    }

}

/**
 * TODO
 */
class ClassMethod1<B : D, D : C, C : Any, P1, R>(
        definedBy : KClass<D>,
        boundTo : KClass<B>,
        private val body : ClassWithSuper<out C>.(P1) -> R
) : ClassMethod<B, D, C, R, (P1) -> R>(definedBy, boundTo) {

    override fun <B2 : D> rebind(cls : KClass<B2>) : ClassMethod<B2, D, C, R, (P1) -> R> {
        return ClassMethod1(definedBy, cls, body)
    }

    override fun getFBound(cls : ClassWithSuper<B>) : (P1) -> R {
        return { cls.body(it) }
    }

}

// region Non-Reified Constructors

/**
 * TODO
 */
fun <D : C, C : Any, R> classMethod(
        cls : KClass<D>,
        body: ClassMethod<C, R, ClassMethod0<C, R>>.ClassWithSuper<out D>.() -> R
) : ClassMethod0<C, R> {
    return ClassMethod0(
            cls,
            cls,
            null,
            body as ClassMethod<C, R, ClassMethod0<C, R>>.ClassWithSuper<out C>.() -> R
    )
}

/**
 * TODO
 */
fun <D : C, C : Any, P1, R> classMethod(
        cls : KClass<D>,
        body: ClassMethod<C, R, ClassMethod1<C, P1, R>>.ClassWithSuper<out D>.(P1) -> R
) : ClassMethod1<C, P1, R> {
    return ClassMethod1(
            cls,
            cls,
            null,
            body as ClassMethod<C, R, ClassMethod1<C, P1, R>>.ClassWithSuper<out C>.(P1) -> R
    )
}

// endregion

// region Reified Constructors

/**
 * TODO
 */
inline fun <reified D : C, C : Any, R> classMethod(
        noinline body: ClassMethod<C, R, ClassMethod0<C, R>>.ClassWithSuper<out D>.() -> R
) : ClassMethod0<C, R> {
    return classMethod(D::class, body)
}

/**
 * TODO
 */
inline fun <reified D : C, C : Any, P1, R> classMethod(
        noinline body: ClassMethod<C, R, ClassMethod1<C, P1, R>>.ClassWithSuper<out D>.(P1) -> R
) : ClassMethod1<C, P1, R> {
    return classMethod(D::class, body)
}

// endregion
*/
