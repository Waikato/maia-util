package maia.util.error

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass

/**
 * Error for when an attempt is made to re-use a delegate property
 * that is designed for single use.
 */
class RedelegationError(
        cls : KClass<*>
) : Exception("Attempted to re-use delegate property of type '${cls.qualifiedName}'; " +
        "create a new instance instead") {

    constructor(property : ReadOnlyProperty<*, *>) : this(property::class)

    constructor(property : ReadWriteProperty<*, *>) : this(property::class)

}
