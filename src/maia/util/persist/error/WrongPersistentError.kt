package maia.util.persist.error

import kotlin.reflect.KClass

/**
 * Error for when trying to get the
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class WrongPersistentError(
    persistenceCls: KClass<*>,
    persistentCls: KClass<*>,
    reason: String,
    requestedCls: KClass<*>
): PersistError(
    "${persistenceCls.qualifiedName} persists ${persistentCls.qualifiedName}, which $reason ${requestedCls.qualifiedName}"
)
