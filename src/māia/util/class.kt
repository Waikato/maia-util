package māia.util

/*
 * Utilities for working with classes.
 */

import māia.util.property.CachedReadOnlyProperty
import java.net.URL
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.superclasses

/**
 * Gets a Kotlin class by name.
 *
 * @param name
 *          The qualified name of the class.
 * @return
 *          The class.
 */
fun <T : Any> classForName(name : String) : KClass<out T> {
    @Suppress("UNCHECKED_CAST")
    return Class.forName(name).kotlin as KClass<out T>
}

/**
 * Checks if a class is a sub-class of another.
 *
 * @param possibleParent
 *          The class to check for super-typing.
 * @return
 *          True if the class is a sub-class.
 */
infix fun KClass<*>.isSubClassOf(possibleParent: KClass<*>) : Boolean {
    return this == possibleParent || allSuperclasses.contains(possibleParent)
}

/**
 * Checks if a class is not a sub-class of another.
 *
 * @param possibleParent
 *          The class to check for super-typing.
 * @return
 *          True if the class is not a sub-class.
 */
infix fun KClass<*>.isNotSubClassOf(possibleParent: KClass<*>) : Boolean {
    return !(this isSubClassOf possibleParent)
}

/**
 * Helper extension property to get the Kotlin class of an object.
 */
val <T : Any> T.kotlinClass : KClass<T>
    get() = javaClass.kotlin

/**
 * Whether the class is an interface class.
 */
val KClass<*>.isInterface : Boolean
    get() = java.isInterface

/**
 * The direct (non-interface) super-class to this type.
 */
val KClass<*>.superClass : KClass<*> by CachedReadOnlyProperty(
    cacheInitialiser = {
        superclasses.first { !it.isInterface }
    },
    perObject = false
)

/**
 * Gets a list of direct super-classes to the receiver which are sub-types
 * of the given class.
 *
 * @param cls
 *          The base class to filter on.
 * @return
 *          The list of direct super-classes which sub-type [cls].
 */
fun KClass<*>.superClassesUpTo(cls : KClass<*>) : List<KClass<*>> {
    if (this isNotSubClassOf cls) return EmptyList
    return superclasses.filter { it isSubClassOf cls }
}

/**
 * Equivalent to [Class.getResource] from a static context.
 *
 * @param  name
 *          The name of the desired resource.
 * @return  A  [java.net.URL] object or null if no resource with this name
 *          is found.
 */
fun getResourceStatic(name: String): URL? {
    class Dummy
    return Dummy::class.java.getResource(name)
}
