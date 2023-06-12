package maia.util.persist.error

import kotlin.reflect.KClass

/**
 * Error for when an attempt is made to attach a [PersistenceRegistry][maia.util.persist.PersistenceRegistry]
 * to anything except the companion object of the class for which the registry was made.
 *
 * @param cls
 *          The class for which the registry was made.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class RegistryNotAttachToCompanionError(
    cls: KClass<*>
): PersistConfigurationError(
    "Persistence registry for class ${cls.qualifiedName} is not attached to the class' companion object"
)
