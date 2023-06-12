package maia.util.persist.error

/**
 * Base class for persistence errors which indicate that the persistence
 * interfaces have been configured incorrectly.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
abstract class PersistConfigurationError(
    message: String? = null,
    cause: Throwable? = null
): PersistError(message, cause)
