package maia.util.persist

import maia.util.persist.error.NotPersistentError
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance


/**
 * Indicates that instances of type P can be persisted. Should be implemented
 * on P's companion object.
 *
 * @param P
 *          The type of the persistent.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
interface Persistent<P: Any> {
    /** The [persistence registry][PersistenceRegistry] for type P. */
    val persistenceRegistry: PersistenceRegistry<P>
}

/**
 * Gets the [PersistenceRegistry] for a particular class, if it is persistent
 * (i.e. its companion object implements [Persistent]).
 *
 * @param P The type of [persistent][Persistent].
 * @receiver The class of [P].
 */
val <P: Any> KClass<P>.persistenceRegistry: PersistenceRegistry<P> get() {
    // Make sure the companion object implements Persistent
    val companion = companionObjectInstance
    if (companion !is Persistent<*>) throw NotPersistentError(this)

    // Safety: Ensured by registry construction process
    @Suppress("UNCHECKED_CAST")
    return companion.persistenceRegistry as PersistenceRegistry<P>
}
