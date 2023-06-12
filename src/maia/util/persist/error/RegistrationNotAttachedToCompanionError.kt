package maia.util.persist.error

import kotlin.reflect.KClass

/**
 * Error for when an attempt is made to attach a [Registration][maia.util.persist.PersistenceRegistry.Registration]
 * to anything except the companion object of the class for which the registration was made.
 *
 * @param cls The class for which the registration was made.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class RegistrationNotAttachToCompanionError(
    cls: KClass<*>
): PersistConfigurationError(
    "Persistence registration for class ${cls.qualifiedName} is not attached to the class' companion object"
)
