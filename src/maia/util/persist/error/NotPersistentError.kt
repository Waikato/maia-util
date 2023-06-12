package maia.util.persist.error

import maia.util.persist.Persistent
import kotlin.reflect.KClass

/**
 * Error for when a class that is not set up for persistence is used in a persisting context.
 *
 * @param cls
 *          The class that is not persistent.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class NotPersistentError(
    cls: KClass<*>
): PersistError(
    "${cls.qualifiedName} is not persistent. To make a class persistent, implement ${Persistent::class.qualifiedName} " +
            "on ${cls.qualifiedName}'s companion object"
)
