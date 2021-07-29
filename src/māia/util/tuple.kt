package māia.util

import māia.util.property.CachedReadOnlyProperty

/**
 * Returns the pair (first, second) of this triple.
 *
 * TODO: params
 */
val <A, B, C> Triple<A, B, C>.leftPair by CachedReadOnlyProperty<Triple<A, B, C>, Pair<A, B>>(
        cacheInitialiser = {
            Pair(this.first, this.second)
        },
        perObject = true
)
