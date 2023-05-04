package maia.util.functionality_provider

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import maia.util.sync

/**
 * [Functionality provider][FunctionalityProvider] which provides for-each style
 * iteration functionality.
 *
 * @param E
 *          The type of each element in the iteration.
 */
class ForEachProvider<out E>(
    private val getIterator : () -> Iterator<E>,
    private val getFlow: () -> Flow<E>,
):
    SyncFunctionalityProvider<(E) -> Unit, Unit>,
    AsyncFunctionalityProvider<(E) -> Unit, Unit>
{

    companion object {
        const val DEFAULT_CAPACITY = Channel.RENDEZVOUS
        val DEFAULT_ON_BUFFER_OVERFLOW = BufferOverflow.SUSPEND

        /**
         * Creates a [ForEachProvider] by lazily retrieving an iterator.
         * Async provision is to iterate the iterator inside a coroutine.
         *
         * @param getIterator
         *          Lazily retrieves the iterator.
         *
         * @return The [ForEachProvider].
         */
        fun <E> fromGetIterator(
            getIterator: (() -> Iterator<E>)
        ): ForEachProvider<E> {
            return ForEachProvider(
                getIterator,
                {
                    flow {
                        for (e in getIterator()) emit(e)
                    }
                }
            )
        }

        /**
         * Creates a [ForEachProvider] by iterating an iterator.
         * Async provision is to iterate the iterator inside a coroutine.
         *
         * @param iterator
         *          The iterator.
         *
         * @return The [ForEachProvider].
         */
        fun <E> fromIterator(
            iterator: Iterator<E>
        ): ForEachProvider<E> {
            return fromGetIterator { iterator }
        }

        /**
         * Creates a [ForEachProvider] by lazily retrieving a flow.
         * Sync provision is start a daemon coroutine to drain the flow into
         * a channel, then (blocking) read elements from the channel.
         *
         * @param capacity
         *          The capacity of the buffering channel. See [Channel].
         * @param onBufferOverflow
         *          Overflow strategy for the buffering channel. See [BufferOverflow].
         * @param getFlow
         *          Lazily retrieves the flow.
         *
         * @return The [ForEachProvider].
         */
        fun <E> fromGetFlow(
            capacity: Int = DEFAULT_CAPACITY,
            onBufferOverflow: BufferOverflow = DEFAULT_ON_BUFFER_OVERFLOW,
            getFlow: (() -> Flow<E>)
        ): ForEachProvider<E> {
            return ForEachProvider(
                {
                    getFlow().sync(capacity, onBufferOverflow)
                },
                getFlow
            )
        }

        /**
         * Creates a [ForEachProvider] by collecting a flow.
         * Sync provision is start a daemon coroutine to drain the flow into
         * a channel, then (blocking) read elements from the channel.
         *
         * @param capacity
         *          The capacity of the buffering channel. See [Channel].
         * @param onBufferOverflow
         *          Overflow strategy for the buffering channel. See [BufferOverflow].
         * @param flow
         *          The flow.
         *
         * @return The [ForEachProvider].
         */
        fun <E> fromFlow(
            capacity: Int = DEFAULT_CAPACITY,
            onBufferOverflow: BufferOverflow = DEFAULT_ON_BUFFER_OVERFLOW,
            flow: Flow<E>
        ): ForEachProvider<E> {
            return fromGetFlow(capacity, onBufferOverflow) { flow }
        }
    }

    override fun provideSync(args : (E) -> Unit) {
        for (e in getIterator()) args(e)
    }

    override suspend fun provideAsync(args : (E) -> Unit) {
        getFlow().collect { args(it) }
    }
}
