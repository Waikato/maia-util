package maia.util.persist

import maia.util.persist.error.NotAPersistenceError
import maia.util.persist.error.NotRegisteredError
import maia.util.persist.error.WrongPersistentError


/**
 * Persists a persistent as the [registered type][T].
 *
 * @param P The type of the [Persistent] to persist.
 * @param T The type of the [persistence][Persists].
 * @receiver The value to persist.
 * @return The persisted value.
 * @throws NotAPersistenceError If [T]'s companion object doesn't implement [Persists].
 * @throws WrongPersistentError If [T]'s companion object implements [Persists] for some type that is not a supertype of [P].
 * @throws NotRegisteredError If [T] is not registered with [P]'s [persistence registry][PersistenceRegistry].
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
inline fun <reified P: Any, reified T: Any> P.persist(): T =
    T::class.persistsCompanionTypedIn<P, T>().persistenceRegistration.persist(this)

