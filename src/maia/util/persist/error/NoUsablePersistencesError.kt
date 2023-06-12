package maia.util.persist.error

import kotlin.reflect.KClass

/**
 * Error for when a [persistence registry][maia.util.persist.PersistenceRegistry] doesn't contain
 * any non-deprecated persistence formats.
 *
 * @param cls The class of the persistent that the registry is for.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class NoUsablePersistencesError(
    cls: KClass<*>
): PersistConfigurationError(
    "Persistence registry for ${cls.qualifiedName} contains no non-deprecated persistence formats"
)
