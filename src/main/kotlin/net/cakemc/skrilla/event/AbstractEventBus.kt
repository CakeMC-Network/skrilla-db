package net.cakemc.skrilla.event

import java.util.function.Consumer

abstract class AbstractEventBus {
    abstract fun <T : Event?> dispatch(event: T): T
    abstract fun <T : Event?> register(eventType: Class<T>, listener: Consumer<T>?)
    abstract fun <T : Event?> unregister(eventType: Class<T>, listener: Consumer<T>)
}