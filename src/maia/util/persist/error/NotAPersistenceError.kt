package maia.util.persist.error

import maia.util.persist.Persists
import kotlin.reflect.KClass

/**
 * Error for when a class that is not set up to be a persistence is used in a persisting context.
 *
 * @param cls
 *          The class that is not a persistence.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class NotAPersistenceError(
    cls: KClass<*>
): PersistError(
    "${cls.qualifiedName} is not a persistence. To make a class a persistence, implement ${Persists::class.qualifiedName} " +
            "on ${cls.qualifiedName}'s companion object"
)
