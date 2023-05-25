package maia.util.datastructure

/**
 * Helper class which can be is responsible for tracking
 * concurrent modifications for a data-structure.
 */
class ConcurrentModificationManager {

    /** The structural generation of the data-structure. */
    inline val generation : Long
        get() = generationProxy

    /** Proxy for [generation] which allows us to inline [generation]/
        [performStructuralModification]. It is [PublishedApi] because
        this is a helper class, if you want to mess around with it from
        Java, on your head be it. */
    @PublishedApi
    internal var generationProxy: Long = 0

    /**
     * Performs the given structural modification.
     *
     * @param R     The return type of the modification.
     */
    inline fun <R> performStructuralModification(
        crossinline block: () -> R
    ): R = block().also { generationProxy++ }

    /**
     * Tracker for views of mutable data-structures which should fail-fast
     * when the source data-structure changes its structure.
     *
     * @param inner
     *          If the source structure is itself a view, set this to a View
     *          from the source's source.
     */
    inner class View(
        val inner: View? = null
    ) {
        @PublishedApi
        internal val source = this@ConcurrentModificationManager

        /** The generation the data-structure was at when this dependent view was created. */
        val expectedGeneration : Long = this@ConcurrentModificationManager.generation

        /** Checks for structural modification of the source data-structure before
            performing some action. */
        inline fun <R> checkForStructuralModification(
            block: () -> R
        ): R {
            // Recursively check all nested views for modification
            var view: View? = this
            while (view != null) {
                if (view.source.generation != view.expectedGeneration)
                    throw ConcurrentModificationException()
                view = view.inner
            }

            return block()
        }
    }

}
