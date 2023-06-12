package maia.util.persist

import kotlinx.serialization.KSerializer
import maia.util.isSubClassOf
import maia.util.persist.error.NotAPersistenceError
import maia.util.persist.error.NotRegisteredError
import maia.util.persist.error.WrongPersistentError
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSuperclassOf

/**
 * Indicates that type T is a persistence of type P. Should be implemented on T's
 * companion object.
 *
 * @param P The type of the persistent.
 * @param T The type of the persistence.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
interface Persists<P: Any, T: Any> {

    /**
     * Resumes the persistent in the state represented by a persistence.
     *
     * @param persistence A previously-persisted representation of the state of a persistent.
     * @return The persistent in its previously-persisted state.
     */
    fun resume(persistence: T): P

    /**
     * The registration of the persistence-type with [persistence registry][PersistenceRegistry]
     * of the [persistent][Persistent].
     */
    val persistenceRegistration: PersistenceRegistry.Registration<P, T>

}

/**
 * Gets the companion object of a persistence-type, ensuring it implements [Persists].
 *
 * @receiver The class of the type to get the companion for.
 * @return The class' companion object, typed as a [Persists].
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 */
fun <T: Any> KClass<T>.persistsCompanion(): Persists<*, T> {
    // Make sure the companion object implements Persists
    val companion = companionObjectInstance
    if (companion !is Persists<*, *>) throw NotAPersistenceError(this)

    // Safety: If you can get the registration without error, its construction
    // ensures that the T type is correctly specified
    companion.persistenceRegistration
    @Suppress("UNCHECKED_CAST")
    return companion as Persists<*, T>

}

/**
 * Gets the companion object of a persistence-type, ensuring it implements [Persists] for some persistent-type P.
 *
 * @receiver The class of the type of the [persistence][Persists].
 * @param P The type of the [Persistent] to assert.
 * @param T The type of the [persistence][Persists].
 * @return The receiving class' companion object, typed as a [Persists].
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 * @throws WrongPersistentError If the receiver's companion object implements [Persists] for some type other than P.
 */
inline fun <reified P: Any, T: Any> KClass<T>.persistsCompanionTyped(): Persists<P, T> = persistsCompanionTyped(P::class)

/**
 * Gets the companion object of a persistence-type, ensuring it implements [Persists] for some persistent-type P.
 *
 * @receiver The class of the type of the [persistence][Persists].
 * @param P The type of the [Persistent] to assert.
 * @param T The type of the [persistence][Persists].
 * @param cls The class of the type of the [Persistent] to assert.
 * @return The receiving class' companion object, typed as a [Persists].
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 * @throws WrongPersistentError If the receiver's companion object implements [Persists] for some type other than P.
 */
fun <P: Any, T: Any> KClass<T>.persistsCompanionTyped(cls: KClass<P>): Persists<P, T> {
    // Check the companion implements Persist for some type
    val companion = persistsCompanion()

    // Safety: Check that type is P
    if (companion.persistenceRegistration.persistentCls != cls)
        throw WrongPersistentError(
            this,
            companion.persistenceRegistration.persistentCls,
            "is not",
            cls
        )
    @Suppress("UNCHECKED_CAST")
    return companion as Persists<P, T>
}

/**
 * Gets the companion object of a persistence-type, ensuring it implements [Persists] for some subtype of P.
 *
 * @receiver The class of the type of the [persistence][Persists].
 * @param P The type of the [Persistent] to assert.
 * @param T The type of the [persistence][Persists].
 * @return The receiving class' companion object, typed as a [Persists].
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 * @throws WrongPersistentError If the receiver's companion object implements [Persists] for some type that is not a subtype of P.
 */
inline fun <reified P: Any, T: Any> KClass<T>.persistsCompanionTypedOut(): Persists<out P, T> = persistsCompanionTypedOut(P::class)

/**
 * Gets the companion object of a persistence-type, ensuring it implements [Persists] for some subtype of P.
 *
 * @receiver The class of the type of the [persistence][Persists].
 * @param P The type of the [Persistent] to assert.
 * @param T The type of the [persistence][Persists].
 * @param cls The class of the type of the [Persistent] to assert.
 * @return The receiving class' companion object, typed as a [Persists].
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 * @throws WrongPersistentError If the receiver's companion object implements [Persists] for some type that is not a subtype of P.
 */
fun <P: Any, T: Any> KClass<T>.persistsCompanionTypedOut(cls: KClass<P>): Persists<out P, T> {
    // Check the companion implements Persist for some type
    val companion = persistsCompanion()

    // Safety: Check that type is a subtype of P
    if (!companion.persistenceRegistration.persistentCls.isSubClassOf(cls))
        throw WrongPersistentError(
            this,
            companion.persistenceRegistration.persistentCls,
            "is not a subtype of",
            cls
        )
    @Suppress("UNCHECKED_CAST")
    return companion as Persists<out P, T>
}

/**
 * Gets the companion object of a persistence-type, ensuring it implements [Persists] for some supertype of P.
 *
 * @receiver The class of the type of the [persistence][Persists].
 * @param P The type of the [Persistent] to assert.
 * @param T The type of the [persistence][Persists].
 * @return The receiving class' companion object, typed as a [Persists].
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 * @throws WrongPersistentError If the receiver's companion object implements [Persists] for some type that is not a supertype of P.
 */
inline fun <reified P: Any, T: Any> KClass<T>.persistsCompanionTypedIn(): Persists<in P, T> = persistsCompanionTypedIn(P::class)

/**
 * Gets the companion object of a persistence-type, ensuring it implements [Persists] for some supertype of P.
 *
 * @receiver The class of the type of the [persistence][Persists].
 * @param P The type of the [Persistent] to assert.
 * @param T The type of the [persistence][Persists].
 * @param cls The class of the type of the [Persistent] to assert.
 * @return The receiving class' companion object, typed as a [Persists].
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 * @throws WrongPersistentError If the receiver's companion object implements [Persists] for some type that is not a supertype of P.
 */
fun <P: Any, T: Any> KClass<T>.persistsCompanionTypedIn(cls: KClass<P>): Persists<in P, T> {
    // Check the companion implements Persist for some type
    val companion = persistsCompanion()

    // Safety: Check that type is a supertype of P
    if (!companion.persistenceRegistration.persistentCls.isSuperclassOf(cls))
        throw WrongPersistentError(
            this,
            companion.persistenceRegistration.persistentCls,
            "is not a supertype of",
            cls
        )
    @Suppress("UNCHECKED_CAST")
    return companion as Persists<in P, T>
}

/**
 * Resumes the persistent in the state represented by this persistence.
 *
 * @receiver A [previously-persisted representation][Persists] of the state of a [Persistent].
 * @param P The type of the [Persistent] to resume.
 * @param T The type of the [persistence][Persists].
 * @return The persistent in its previously-persisted state.
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 * @throws WrongPersistentError If the receiver's companion object implements [Persists] for some type that is not a subtype of P.
 */
inline fun <reified P: Any, reified T: Any> T.resume(): P =
    T::class.persistsCompanionTypedOut<P, T>().resume(this)

/**
 * Gets the [serialiser][KSerializer] to use for serializing this persistence.
 *
 * @receiver A persistence of some type.
 * @return The persistence's serialiser.
 *
 * @throws NotAPersistenceError If the receiver's companion object doesn't implement [Persists].
 * @throws NotRegisteredError If the persistence is not registered.
 */
inline fun <reified T: Any> T.persistenceSerializer(): KSerializer<T> =
    T::class.persistsCompanion().persistenceRegistration.serializer
