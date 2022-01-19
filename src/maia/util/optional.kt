package maia.util

/*
 * Package defining the Optional type, which represents when a value may
 * or may not be present. Better than java.util.Optional<T> as it doesn't use
 * null to represent absence, therefore allowing nullable types to be supported.
 */

/**
 * Type representing values that may or may not be present.
 *
 * @param T
 *          The type of value, if it is present.
 */
sealed class Optional<out T> {

    /**
     * Gets the value if it is present, or throws if it is not.
     *
     * @return
     *          The value if present.
     * @throws NoSuchElementException
     *          If the value is not present.
     */
    abstract fun get() : T

    /**
     * Gets the value if it is present, or null if it is not.
     *
     * @return
     *          The value if present, or null if not.
     */
    abstract fun getOrNull() : T?

}

/**
 * The value of an optional type when it is not present.
 */
object Absent : Optional<Nothing>() {
    override fun get() : Nothing = throw NoSuchElementException()
    override fun getOrNull() : Nothing? = null
    override fun toString() : String = "<Absent>"
}

/**
 * A wrapper for optional values which are present. Compares equal to
 * the underlying value.
 *
 * @param value
 *          The value.
 * @param T
 *          The type of value.
 */
class Present<T>(val value : T) : Optional<T>() {
    override fun get() : T = value
    override fun getOrNull() : T? = value
    override fun toString() : String = value.toString()
    override fun equals(other : Any?) : Boolean = value == if (other is Present<*>) other.value else other
    override fun hashCode() : Int = value.hashCode()
}

/**
 * Helper extension to convert a value of any type to a [Present] of that
 * type with that value.
 */
val <T> T.asOptional: Present<T>
    get() = Present(this)

/**
 * Helper extension to convert a value of any type to an [Optional] of that
 * type with that value. The result will be a [Present] if the value is
 * non-null, or [Absent] if the value is null.
 */
val <T> T?.asNullAbsentOptional: Optional<T>
    get() = if (this == null) Absent else Present(this)
