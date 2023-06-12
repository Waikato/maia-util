package maia.util.persist.error

import kotlin.reflect.KClass

/**
 * Error for when an attempt is made to register the same persistence multiple times.
 *
 * @param cls The class for which multiple attempts were made.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class MultipleRegistrationsError(
    cls: KClass<*>
): PersistConfigurationError(
    "Attempted to register ${cls.qualifiedName} multiple times"
)
