package maia.util.error

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass

/**
 * Error for when an attempt is made to use a delegate property
 * before it has been delegated to.
 */
class PreDelegationError(
        cls : KClass<*>
) : Exception("Attempted to use delegate property of type '${cls.qualifiedName}' " +
        "before delegation has occurred") {

    constructor(property : ReadOnlyProperty<*, *>) : this(property::class)

    constructor(property : ReadWriteProperty<*, *>) : this(property::class)

}
