package maia.util.functionality_provider

import kotlinx.coroutines.runBlocking

/**
 * Base interface for classes which provide functionality in either/both sync
 * and async scenarios. Classes indicate that they can natively provide their
 * functionality synchronously by implementing [SyncFunctionalityProvider], and
 * asynchronously by implementing [AsyncFunctionalityProvider].
 *
 * @param P
 *          The type of the arguments to parameterize the functionality.
 * @param R
 *          The return type of the functionality.
 */
sealed interface FunctionalityProvider<in P, out R>

/**
 * [Functionality provider][FunctionalityProvider] which can natively provide
 * its functionality synchronously.
 *
 * @param P
 *          The type of the arguments to parameterize the functionality.
 * @param R
 *          The return type of the functionality.
 */
interface SyncFunctionalityProvider<in P, out R>: FunctionalityProvider<P, R> {
    /**
     * Provides the functionality synchronously.
     *
     * @param args
     *          The arguments to parameterize the functionality.
     * @return
     *          The result of providing the functionality.
     */
    fun provideSync(args: P): R
}

/**
 * [Functionality provider][FunctionalityProvider] which can natively provide
 * its functionality asynchronously.
 *
 * @param P
 *          The type of the arguments to parameterize the functionality.
 * @param R
 *          The return type of the functionality.
 */
interface AsyncFunctionalityProvider<in P, out R>: FunctionalityProvider<P, R> {
    /**
     * Provides the functionality asynchronously.
     *
     * @param args
     *          The arguments to parameterize the functionality.
     * @return
     *          The result of providing the functionality.
     */
    suspend fun provideAsync(args: P): R
}

/**
 * Utilises functionality from a provider in a synchronous fashion, either
 * outside of a coroutine context, or inside a coroutine context where we
 * want to avoid suspending.
 *
 * @receiver The [provider of the functionality][FunctionalityProvider] to utilise.
 * @param args
 *          The arguments to parameterize the functionality.
 * @return
 *          The result of provided by the functionality.
 */
fun <P, R> FunctionalityProvider<P, R>.utiliseSync(args: P): R {
    return when (this) {
        // Prefer native synchronous implementation, if available
        is SyncFunctionalityProvider<P, R> -> provideSync(args)
        // Otherwise, block on the async implementation
        is AsyncFunctionalityProvider<P, R> -> runBlocking { provideAsync(args) }
    }
}

/**
 * Utilises functionality from a provider in an asynchronous fashion, where we
 * want to suspend where possible.
 *
 * @receiver The [provider of the functionality][FunctionalityProvider] to utilise.
 * @param args
 *          The arguments to parameterize the functionality.
 * @return
 *          The result of provided by the functionality.
 */
suspend fun <P, R> FunctionalityProvider<P, R>.utiliseAsync(args: P): R {
    return when (this) {
        // Prefer native asynchronous implementation, if available
        is AsyncFunctionalityProvider<P, R> -> provideAsync(args)
        // Otherwise, use sync implementation
        is SyncFunctionalityProvider<P, R> -> provideSync(args)
    }
}
