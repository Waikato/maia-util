package maia.util.persist.error

/**
 * Base class for all persistence-related errors.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
abstract class PersistError(
    message: String? = null,
    cause: Throwable? = null
): Exception(message, cause)
