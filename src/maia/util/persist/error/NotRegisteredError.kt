package maia.util.persist.error

import kotlin.reflect.KClass

/**
 * Error for when trying to use an unregistered persistence.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class NotRegisteredError(
    persistenceCls: KClass<*>,
    persistentCls: KClass<*>
): PersistConfigurationError(
    "Persistence ${persistenceCls.qualifiedName} is not registered with its persistent. " +
            "Add it in ${persistentCls.qualifiedName}'s companion object's persistence registry."
)
