package maia.util.persist.error

import kotlin.reflect.KClass

/**
 * Error for when an attempt is made to create multiple persistence registries for the same class.
 *
 * @param cls
 *          The class for which multiple attempts were made.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class MultipleRegistriesError(
    cls: KClass<*>
): PersistConfigurationError(
    "Attempted to create multiple persistence registries for class ${cls.qualifiedName}"
)
