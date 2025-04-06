package net.cakemc.database.callbacks

/**
 * The interface Conditional consumer.
 *
 * @param <T> the type parameter
</T> */
fun interface ConditionalConsumer<T> {

    /**
     * Expect boolean.
     *
     * @param value the value
     * @return the boolean
     */
    fun expect(value: T): Boolean

}
