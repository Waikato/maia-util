package māia.util.datastructure

/*
 * Types for managing the concurrent modification of collections.
 */

import java.lang.ref.WeakReference

/**
 * Interface for classes which can be notified that a related object has
 * been modified.
 */
interface ConcurrentModificationNotifiable {

    /**
     * Notifies of a modification in the related object.
     */
    fun notifyOfConcurrentModification()

}

/**
 * Helper delegate which handles receiving the concurrent-modification
 * notification, and provides the ability to check if it has been received.
 */
class ConcurrentModificationNotifiableDelegate : ConcurrentModificationNotifiable {

    /** Whether notifications are currently being listened for. */
    var listening = true

    /** Whether the related object has been modified during our lifetime. */
    var concurrentModification = false
        private set

    override fun notifyOfConcurrentModification() {
        concurrentModification = concurrentModification || listening
    }

    /**
     * Performs the given action under the assurance that the related
     * object hasn't been modified.
     *
     * @param block
     *          The action to perform.
     * @return
     *          The result of the [block].
     * @param R
     *          The return-type of the [block].
     * @throws ConcurrentModificationException
     *          If a concurrent modification notification has been received.
     */
    inline fun <R> ensureNoConcurrentModification(block : () -> R) : R {
        // Throw an exception if the notification has been received
        if (concurrentModification) throw ConcurrentModificationException()

        // Stop listening for notifications so if we perform modifications
        // inside the block, we won't be notified of them.
        listening = false

        try {
            return block()
        } finally {
            // Resume listening for modifications
            listening = true
        }
    }

}

/**
 * Class which manages a collection of [ConcurrentModificationNotifiable]s,
 * and notifies them in bulk.
 */
class ConcurrentModificationNotifiableManager {

    /** The managed notifiables. */
    private val notifiables = ArrayList<WeakReference<ConcurrentModificationNotifiable>>()

    /**
     * Registers a new [ConcurrentModificationNotifiable] with the manager.
     *
     * @param notifiable
     *          The notifiable to manage.
     */
    fun registerNotifiable(notifiable : ConcurrentModificationNotifiable) {
        // Clean any dead references
        cleanNotifiables()

        // Add a reference to the notifiable
        notifiables.add(WeakReference(notifiable))
    }

    /**
     * Notifies all registered notifiables of a concurrent modification.
     */
    fun notifyAllManaged() = forEachRemainingNotifiable {
        it.notifyOfConcurrentModification()
    }

    /**
     * Removes any dead notifiable references from the manager.
     */
    private fun cleanNotifiables() = forEachRemainingNotifiable {
        // Do nothing
    }

    /**
     * Iterates through the managed notifiables, performing the given action
     * on each one that is still alive. Removes any dead references encountered
     * during iteration.
     *
     * @param block
     *          The action to perform on each living notifiable.
     */
    private fun forEachRemainingNotifiable(block : (ConcurrentModificationNotifiable) -> Unit) {
        // Get an iterator over the references
        val iterator = notifiables.iterator()

        // Handle each reference in turn
        while (iterator.hasNext()) {
            // Get the reference to the next notifiable
            val reference = iterator.next()

            // Get the notifiable from the reference
            val notifiable = reference.get()

            // If the reference is dead, discard it
            if (notifiable == null) {
                iterator.remove()
                continue
            }

            // Perform the action
            block(notifiable)
        }
    }

}
