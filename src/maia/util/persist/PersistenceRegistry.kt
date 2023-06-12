package maia.util.persist

import kotlinx.serialization.KSerializer
import maia.util.persist.error.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObjectInstance

/**
 * A registry for the persistence formats for a persistent type. Only one
 * instance can exist per type [P].
 *
 * @param P The type of [persistent][Persistent] that the registry is for.
 * @param cls The class of type P.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class PersistenceRegistry<P: Any> private constructor(val cls: KClass<P>): ReadOnlyProperty<Persistent<P>, PersistenceRegistry<P>> {
    /** The [serialisers][KSerializer] for each class that persists [P]. */
    private val registrations = HashMap<KClass<*>, KSerializer<*>>()

    /** The default format to use for persistence. */
    private lateinit var default: KClass<*>

    override fun getValue(thisRef: Persistent<P>, property: KProperty<*>): PersistenceRegistry<P> = this

    /**
     * Represents the state of the registration between a type that persists [P]
     * and the [persistence registry][PersistenceRegistry] for that type.
     *
     * @param P The type of the persistent.
     * @param T The type that persists [P].
     * @param persistentCls The class of the persistent [P].
     * @param persistenceCls The class of the persistence [T].
     */
    class Registration<P: Any, T: Any> private constructor(
        val persistentCls: KClass<P>,
        val persistenceCls: KClass<T>
    ): ReadOnlyProperty<Persists<P, T>, Registration<P, T>> {
        /** The [serialiser][KSerializer] for [T], once it's registered. */
        internal lateinit var _serializer: KSerializer<T>
        /** The function which persists a [P] as a [T], once it's registered. */
        internal lateinit var _persistFn: P.() -> T

        /** The [serialiser][KSerializer] for [T], once it's registered. */
        val serializer: KSerializer<T> get() = ensureRegistered { _serializer }

        /** Whether this registration has been completed. */
        val isRegistered: Boolean get() = this::_serializer.isInitialized

        /**
         * Checks that the registration has been completed before doing something.
         *
         * @param R The return type of the thing to do after checking the registration.
         * @param block The thing to do after checking the registration.
         * @return The result of [block].
         * @throws NotRegisteredError If the registration is not complete.
         */
        inline fun <R> ensureRegistered(block: () -> R): R {
            if (!isRegistered) throw NotRegisteredError(persistenceCls, persistentCls)
            return block()
        }

        /**
         * Persists a persistent as the [registered type][T].
         *
         * @param value The value to persist.
         * @return The persisted value.
         * @throws NotRegisteredError If the registration is not complete.
         */
        fun persist(value: P): T = ensureRegistered {
            return _persistFn.invoke(value)
        }

        override fun getValue(thisRef: Persists<P, T>, property: KProperty<*>): Registration<P, T> = this

        /**
         * Provides this registration as a delegate to the companion object of the persistence [T].
         */
        inner class DelegateProvider {
            operator fun provideDelegate(thisRef: Persists<P, T>, property: KProperty<*>): Registration<P, T> {
                // Ensure we are being provided to the companion object of the persistence class.
                if (thisRef !== this@Registration.persistenceCls.companionObjectInstance)
                    throw RegistrationNotAttachToCompanionError(this@Registration.persistenceCls)

                return this@Registration
            }
        }

        companion object {

            /** All classes registered with any [PersistenceRegistry]. */
            private val registeredClasses = HashSet<KClass<*>>()

            /**
             * Static constructor for [Registration] objects.
             *
             * @param P The type of the persistent.
             * @param T The type that persists [P].
             * @return A [delegate provider][DelegateProvider] for the registration.
             * @throws MultipleRegistrationsError If [persistenceCls] has already been registered.
             */
            inline operator fun <reified P: Any, reified T: Any> invoke() =
                this.invoke(P::class, T::class)

            /**
             * Static constructor for [Registration] objects.
             *
             * @param P The type of the persistent.
             * @param T The type that persists [P].
             * @param persistentCls The class of the persistent [P].
             * @param persistenceCls The class of the persistence [T].
             * @return A [delegate provider][DelegateProvider] for the registration.
             * @throws MultipleRegistrationsError If [persistenceCls] has already been registered.
             */
            operator fun <P: Any, T: Any> invoke(
                persistentCls: KClass<P>,
                persistenceCls: KClass<T>
            ): Registration<P, T>.DelegateProvider {
                // Make sure this type hasn't been registered already
                if (persistenceCls in registeredClasses) throw MultipleRegistrationsError(persistenceCls)

                val registration = Registration(persistentCls, persistenceCls)

                registeredClasses.add(persistenceCls)

                return registration.DelegateProvider()
            }
        }
    }

    /**
     * Provides the [persistence registry][PersistenceRegistry] as a delegate to the [persistent's][P]
     * companion object.
     */
    inner class DelegateProvider {
        operator fun provideDelegate(thisRef: Persistent<P>, property: KProperty<*>): PersistenceRegistry<P> {
            // Make sure we are providing to the persistent's companion object.
            if (thisRef !== cls.companionObjectInstance)
                throw RegistryNotAttachToCompanionError(cls)

            return this@PersistenceRegistry
        }
    }

    companion object {

        /** All known persistence registries, keyed by the [persistent][Persistent] type. */
        private val registries = HashMap<KClass<*>, PersistenceRegistry<*>>()

        /**
         * Static constructor for [persistence registries][PersistenceRegistry].
         *
         * @param P The type of [persistent][Persistent] that the registry is for.
         * @param block How to configure the registry.
         * @return A [delegate provider][DelegateProvider] of the registry.
         * @throws MultipleRegistriesError If a registry for [P] already exists.
         */
        inline operator fun <reified P: Any> invoke(
            noinline block: Builder<P>.() -> Unit
        ): PersistenceRegistry<P>.DelegateProvider =
            invoke(P::class, block)

        /**
         * Static constructor for [persistence registries][PersistenceRegistry].
         *
         * @param P The type of [persistent][Persistent] that the registry is for.
         * @param cls The class of type [P].
         * @param block How to configure the registry.
         * @return A [delegate provider][DelegateProvider] of the registry.
         * @throws MultipleRegistriesError If a registry for [P] already exists.
         * @throws NoUsablePersistencesError If no non-deprecated persistences were added to the registry.
         */
        operator fun <P: Any> invoke(
            cls: KClass<P>,
            block: Builder<P>.() -> Unit
        ): PersistenceRegistry<P>.DelegateProvider {
            // Only one registry can exist for each type
            if (cls in registries)
                throw MultipleRegistriesError(cls)

            // Create a build-scope and configure it using the client code
            val builder = Builder(cls)
            val registry: PersistenceRegistry<P>
            try {
                builder.block()
            } finally {
                // Make sure the builder is finished, so we don't leak access
                registry = builder.finish()
            }

            registries[cls] = registry

            return registry.DelegateProvider()
        }

    }

    /**
     * Scope for building a [persistence registry][PersistenceRegistry].
     *
     * @param P The type of [persistent][Persistent] that the registry is for.
     * @param cls The class of type [P].
     *
     */
    class Builder<P: Any> internal constructor(cls: KClass<P>) {
        /** The registry being built. Is nulled out once building is finished. */
        private var _registry: PersistenceRegistry<P>? = PersistenceRegistry(cls)
        /** The default [persistence type][Persists] to use if one isn't specified. */
        private var _default: KClass<*>? = null

        /**
         * Gets the registry being built, checking that building is still on-going.
         *
         * @return The [registry][PersistenceRegistry] being built.
         * @throws IllegalStateException If the registry is no longer being built.
         */
        private fun getRegistryChecked(): PersistenceRegistry<P> {
            // If for some reason the reference to the builder is exported from the build-scope,
            // we error on use
            return _registry ?: throw IllegalStateException("Use outside of build")
        }

        /**
         * Adds a [persistence type][Persists] to the registry.
         *
         * @param T The type of the [persistence][Persists].
         * @param serializer A [serialiser][KSerializer] for serialising/deserialising the persistence type.
         * @param persistFn How to create an instance of the persistence from the [persistent][Persistent].
         * @throws IllegalStateException If the registry is no longer being built.
         * @throws MultipleRegistrationsError If [cls] is already registered.
         * @throws NotAPersistenceError If [T]'s companion object doesn't implement [Persists].
         * @throws WrongPersistentError If [T]'s companion object implements [Persists] for some type other than P.
         */
        inline fun <reified T: Any> add(
            serializer: KSerializer<T>,
            noinline persistFn: P.() -> T
        ) = add(T::class, serializer, persistFn)

        /**
         * Adds a [persistence type][Persists] to the registry.
         *
         * @param T The type of the [persistence][Persists].
         * @param cls The class of type [T].
         * @param serializer A [serialiser][KSerializer] for serialising/deserialising the persistence type.
         * @param persistFn How to create an instance of the persistence from the [persistent][Persistent].
         * @throws IllegalStateException If the registry is no longer being built.
         * @throws MultipleRegistrationsError If [cls] is already registered.
         * @throws NotAPersistenceError If [T]'s companion object doesn't implement [Persists].
         * @throws WrongPersistentError If [T]'s companion object implements [Persists] for some type other than P.
         */
        fun <T: Any> add(
            cls: KClass<T>,
            serializer: KSerializer<T>,
            persistFn: P.() -> T
        ) = addInternal(cls, serializer, persistFn, true)

        /**
         * Adds a [persistence type][Persists] to the registry, explicitly specifying that it should be
         * the default persistence type.
         *
         * @param T The type of the [persistence][Persists].
         * @param serializer A [serialiser][KSerializer] for serialising/deserialising the persistence type.
         * @param persistFn How to create an instance of the persistence from the [persistent][Persistent].
         * @throws IllegalStateException If the registry is no longer being built.
         * @throws MultipleRegistrationsError If [cls] is already registered.
         * @throws NotAPersistenceError If [T]'s companion object doesn't implement [Persists].
         * @throws WrongPersistentError If [T]'s companion object implements [Persists] for some type other than P.
         */
        inline fun <reified T: Any, C: Persists<P, T>> default(
            serializer: KSerializer<T>,
            noinline persistFn: P.() -> T
        ) = default(T::class, serializer, persistFn)

        /**
         * Adds a [persistence type][Persists] to the registry, explicitly specifying that it should be
         * the default persistence type.
         *
         * @param T The type of the [persistence][Persists].
         * @param cls The class of type [T].
         * @param serializer A [serialiser][KSerializer] for serialising/deserialising the persistence type.
         * @param persistFn How to create an instance of the persistence from the [persistent][Persistent].
         * @throws IllegalStateException If the registry is no longer being built.
         * @throws MultipleRegistrationsError If [cls] is already registered.
         * @throws NotAPersistenceError If [T]'s companion object doesn't implement [Persists].
         * @throws WrongPersistentError If [T]'s companion object implements [Persists] for some type other than P.
         */
        fun <T: Any> default(
            cls: KClass<T>,
            serializer: KSerializer<T>,
            persistFn: P.() -> T
        ) {
            // No need to update the builder's default as it will be overridden by the registry's default
            // in finish()
            addInternal(cls, serializer, persistFn, false)

            // Set the registry's default format
            // Safety: addInternal will throw if _registry is null
            _registry!!.default = cls
        }

        /**
         * Adds a [persistence type][Persists] to the registry, specifying that it should not be
         * used for new persistences i.e. it only supports restoring previously-saved persistences.
         *
         * @param T The type of the [persistence][Persists].
         * @param serializer A [serialiser][KSerializer] for serialising/deserialising the persistence type.
         * @throws IllegalStateException If the registry is no longer being built.
         * @throws MultipleRegistrationsError If [cls] is already registered.
         * @throws NotAPersistenceError If [T]'s companion object doesn't implement [Persists].
         * @throws WrongPersistentError If [T]'s companion object implements [Persists] for some type other than P.
         */
        inline fun <reified T: Any, C: Persists<P, T>> deprecated(
            serializer: KSerializer<T>
        ) = deprecated(T::class, serializer)

        /**
         * Adds a [persistence type][Persists] to the registry, specifying that it should not be
         * used for new persistences i.e. it only supports restoring previously-saved persistences.
         *
         * @param T The type of the [persistence][Persists].
         * @param cls The class of type [T].
         * @param serializer A [serialiser][KSerializer] for serialising/deserialising the persistence type.
         * @throws IllegalStateException If the registry is no longer being built.
         * @throws MultipleRegistrationsError If [cls] is already registered.
         * @throws NotAPersistenceError If [T]'s companion object doesn't implement [Persists].
         * @throws WrongPersistentError If [T]'s companion object implements [Persists] for some type other than P.
         */
        fun <T: Any, C: Persists<P, T>> deprecated(
            cls: KClass<T>,
            serializer: KSerializer<T>
        ) = addInternal(cls, serializer, { throw PersistenceDeprecatedError(cls) }, false)

        /**
         * Base method for adding a [persistence type][Persists] to the registry.
         *
         * @param T The type of the [persistence][Persists].
         * @param cls The class of type [T].
         * @param serializer A [serialiser][KSerializer] for serialising/deserialising the persistence type.
         * @param persistFn How to create an instance of the persistence from the [persistent][Persistent].
         * @param setDefault Whether to override the current [_default] with this type.
         * @throws IllegalStateException If the registry is no longer being built.
         * @throws MultipleRegistrationsError If [cls] is already registered.
         * @throws NotAPersistenceError If [T]'s companion object doesn't implement [Persists].
         * @throws WrongPersistentError If [T]'s companion object implements [Persists] for some type other than P.
         */
        private fun <T: Any> addInternal(
            cls: KClass<T>,
            serializer: KSerializer<T>,
            persistFn: P.() -> T,
            setDefault: Boolean
        ) {
            // Ensure we are still building
            val registry = getRegistryChecked()

            // Ensure the persistence isn't already registered
            if (cls in registry.registrations) throw MultipleRegistrationsError(cls)

            // Get the companion object for the persistence
            val companion = cls.persistsCompanionTyped(registry.cls)

            // Update its registration
            companion.persistenceRegistration._serializer = serializer
            companion.persistenceRegistration._persistFn = persistFn
            registry.registrations[cls] = serializer

            if (setDefault) this._default = cls
        }

        /**
         * Specifies that the registry has finished being built.
         *
         * @return The finished [persistence registry][PersistenceRegistry].
         * @throws IllegalStateException If the registry is no longer being built.
         * @throws NoUsablePersistencesError If no non-deprecated persistences were added to the registry.
         */
        internal fun finish(): PersistenceRegistry<P> {
            // Set the registry's default format, if not already done explicitly
            val registry = getRegistryChecked()
            if (!registry::default.isInitialized) {
                registry.default = this._default ?: throw NoUsablePersistencesError(registry.cls)
            }

            // Drop references to error on any subsequent use
            this._registry = null
            this._default = null

            return registry
        }
    }

}
