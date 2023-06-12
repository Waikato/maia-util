package maia.util.persist.error

import kotlin.reflect.KClass

/**
 * Error for when trying to persist to a deprecated format.
 *
 * @param cls The class of the [format][maia.util.persist.Persists] that is deprecated.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class PersistenceDeprecatedError(
    cls: KClass<*>
): PersistError("${cls.qualifiedName} is deprecated")
