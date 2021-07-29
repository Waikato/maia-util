package māia.util

/*
 * Utilities for working with reflected constructors.
 */

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType

/**
 * Gets the constructor of a class which takes no arguments.
 *
 * @receiver
 *          The class to find the constructor for.
 * @return
 *          The constructor if the class has one, otherwise null.
 * @param T
 *          The type of the class.
 */
fun <T : Any> KClass<T>.getZeroArgConstructor() : KFunction<T>? {
    return constructors
            .iterator()
            .filter { constructor ->
                constructor.parameters.all { parameter ->
                    parameter.isOptional
                }
            }
            .asIterable()
            .firstOrNull()
}

/**
 * Gets the constructor of a class which takes no arguments.
 *
 * @return
 *          The constructor if the class has one, otherwise null.
 * @param T
 *          The type of the class.
 */
inline fun <reified T : Any> getZeroArgConstructor() : KFunction<T>? {
    return T::class.getZeroArgConstructor()
}

/**
 * Gets the constructor of the class which takes the given types of arguments.
 *
 * @receiver
 *          The class to find the constructor for.
 * @param paramTypes
 *          The types of the arguments to the parameter, in declarative order.
 * @return
 *          The constructor if the class has one, otherwise null.
 * @param T
 *          The type of the class.
 */
fun <T : Any> KClass<T>.getConstructorWithParamTypes(vararg paramTypes : KType) : KFunction<T>? {
    return constructors
            .iterator()
            .filter { constructor ->
                constructor.parameters.size == paramTypes.size
            }
            .filter { constructor ->
                constructor.parameters
                        .enumerate()
                        .all { (index, parameter) ->
                            typesEqualUnderReprojection(parameter.type, paramTypes[index])
                        }
            }
            .asIterable()
            .firstOrNull()
}

/**
 * Gets the constructor of the class which takes the given types of arguments.
 *
 * @param paramTypes
 *          The types of the arguments to the parameter, in declarative order.
 * @return
 *          The constructor if the class has one, otherwise null.
 * @param T
 *          The type of the class.
 */
inline fun <reified T : Any> getConstructorWithParamTypes(
        vararg paramTypes : KType
) : KFunction<T>? {
    return T::class.getConstructorWithParamTypes(*paramTypes)
}
