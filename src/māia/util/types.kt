package māia.util

import kotlin.reflect.*
import kotlin.reflect.full.createType

/**
 * Reified version of [kotlin.reflect.full.createType].
 */
inline fun <reified T : Any> createType(
        arguments: List<KTypeProjection> = emptyList(),
        nullable: Boolean = false,
        annotations: List<Annotation> = emptyList()
) : KType {
    return T::class.createType(arguments, nullable, annotations)
}

/**
 * Creates a type projection for each type parameter to a class,
 * with matching variance.
 *
 * @receiver
 *          The class.
 * @return
 *          The list of type-projections.
 */
fun KClass<*>.createTypeProjectionsForTypeParameters() : List<KTypeProjection> {
    return typeParameters.map {
        when (it.variance) {
            KVariance.INVARIANT -> KTypeProjection.invariant(it.createType())
            KVariance.IN -> KTypeProjection.contravariant(it.createType())
            KVariance.OUT -> KTypeProjection.covariant(it.createType())
        }
    }
}

/**
 * Creates a type where the arguments to the type-parameters are
 * the projections of the parameters themselves.
 *
 * @receiver
 *          The class to create the projected type for.
 * @return
 *          The projected type.
 */
fun KClass<*>.createProjectedType() : KType {
    return createType(createTypeProjectionsForTypeParameters())
}

/**
 * Checks if two generic types are equal, taking into account that they might
 * be using different generic type parameters which are equivalent.
 *
 * @param type1
 *          The first type to compare.
 * @param type2
 *          The second type to compare.
 * @return
 *          Whether the given types are equal up to their type-parameters
 *          being equivalent.
 */
fun typesEqualUnderReprojection(type1 : KType, type2 : KType) : Boolean {
    // Get the classifiers for the two types
    val type1Classifier = type1.classifier
    val type2Classifier = type2.classifier

    // If both types are generic, then they must have the same upper-bounds
    if (type1Classifier is KTypeParameter && type2Classifier is KTypeParameter) {
        return typeParametersHaveSameUpperBoundsUnderReprojection(
                type1Classifier,
                type2Classifier
        )
    }

    // If either classifier is a concrete class and the other is not or is a different
    // concrete class, then the types are not the same
    if (type1Classifier != type2Classifier) return false

    // Otherwise, the types are the same when all of their arguments are
    return zip(type1.arguments, type2.arguments).all { (proj1, proj2) ->
        when {
            // Both arguments are star-projected, therefore the same
            proj1.variance == null && proj2.variance == null -> true

            // Variance is different, therefore arguments are different
            proj1.variance != proj2.variance -> false

            // Same non-star variance, recurse to determine if argument types are the same
            else -> typesEqualUnderReprojection(proj1.type!!, proj2.type!!)
        }
    }
}

/**
 * Checks if two type-parameters have the same upper bounds, given that
 * the upper-bounds might be generic but equivalent.
 *
 * @param typeParameter1
 *          The first type-parameter.
 * @param typeParameter2
 *          The second type-parameter.
 * @return
 *          Whether the upper-bounds of the type-parameters are equal,
 *          up to their own type-parameters being equivalent.
 */
fun typeParametersHaveSameUpperBoundsUnderReprojection(
        typeParameter1 : KTypeParameter,
        typeParameter2 : KTypeParameter
) : Boolean {
    // Get the upper bounds of the two type-parameters
    val upperBounds1 = typeParameter1.upperBounds
    val upperBounds2 = typeParameter2.upperBounds

    // Must have the same number of upper bounds
    if (upperBounds1.size != upperBounds2.size) return false

    return when (upperBounds1.size) {
        // Both are unbounded
        0 -> true

        // A single upper-bound may be another type parameter or an actual type
        1 -> {
            val upperBound1 = upperBounds1[0]
            val upperBound2 = upperBounds2[0]
            val classifier1 = upperBound1.classifier
            if (classifier1 is KTypeParameter) {
                val classifier2 = upperBound2.classifier
                classifier2 is KTypeParameter &&
                        typeParametersHaveSameUpperBoundsUnderReprojection(
                                classifier1, classifier2
                        )
            } else {
                typesEqualUnderReprojection(upperBound1, upperBound2)
            }
        }

        // More than one upper-bound means no type parameters
        else -> {
            // Put the upper-bounds in a canonical order
            val upperBounds1Sorted = Array(upperBounds1.size) { upperBounds1[it] }
            upperBounds1Sorted.sortBy { it.toString() }
            val upperBounds2Sorted = Array(upperBounds2.size) { upperBounds2[it] }
            upperBounds2Sorted.sortBy { it.toString() }

            // Check that the now pair-wise associated upper-bound types are
            // equivalent
            zip(
                    upperBounds1Sorted.iterator(),
                    upperBounds2Sorted.iterator()
            ).all { (upperBound1, upperBound2) ->
                typesEqualUnderReprojection(upperBound1, upperBound2)
            }
        }
    }
}
