package net.cakemc.skrilla.event

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReferenceArray
import java.util.function.Consumer

class DefaultEventBus(
    private val maxListenersPerEvent: Int
) : AbstractEventBus() {

    private val listeners = ConcurrentHashMap<Class<*>, AtomicReferenceArray<Consumer<*>?>>()

    /**
     * Registers a listener for a specific event type.
     *
     * @param eventType the class of the event to listen for.
     * @param listener  the consumer that will handle the event.
     * @param <T>       the type of the event.
     * @throws IllegalStateException if the listener array for the event type is full.
     */
    override fun <T : Event?> register(eventType: Class<T>, listener: Consumer<T>?) {
        listeners.compute(eventType) { key: Class<*>?, existingArray: AtomicReferenceArray<Consumer<*>?>? ->
            if (existingArray == null) {
                val newArray = AtomicReferenceArray<Consumer<*>?>(
                    maxListenersPerEvent
                )
                newArray[0] = listener
                return@compute newArray
            }
            for (i in 0 until maxListenersPerEvent) {
                if (existingArray[i] == null) {
                    existingArray[i] = listener
                    return@compute existingArray
                }
            }
            throw IllegalStateException("Maximum listeners reached for event type: " + eventType.name)
        }
    }

    /**
     * Unregisters a listener for a specific event type.
     *
     * @param eventType the class of the event to stop listening for.
     * @param listener  the consumer to remove from the listener list.
     * @param <T>       the type of the event.
     * @throws IllegalArgumentException if the listener is not registered for the event type.
     */
    override fun <T : Event?> unregister(eventType: Class<T>, listener: Consumer<T>) {
        val registeredListeners = listeners[eventType]
            ?: throw IllegalArgumentException("No listeners registered for event type: " + eventType.name)

        var found = false
        for (i in 0 until maxListenersPerEvent) {
            val existingListener = registeredListeners[i]
            if (existingListener == listener) {
                registeredListeners[i] = null
                found = true
                break
            }
        }

        if (!found) {
            throw IllegalArgumentException("Listener not found for event type: " + eventType.name)
        }
    }

    /**
     * Dispatches an event to all listeners registered for its type.
     *
     * @param event the event object to dispatch.
     * @param <T>   the type of the event.
     */
    override fun <T : Event?> dispatch(event: T): T {
        if (event == null)
            return event

        val eventType: Class<*> = event.javaClass
        val registeredListeners = listeners[eventType]

        if (registeredListeners != null) {
            for (i in 0 until maxListenersPerEvent) {
                val listener = registeredListeners[i] as? Consumer<T>
                listener?.accept(event)
            }
        }

        return event
    }
}