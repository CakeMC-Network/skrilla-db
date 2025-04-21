package net.cakemc.database.callbacks

/**
 * The interface for a consumer that applies a condition to a given value of type [T].
 *
 * This interface is typically used to define custom logic that checks whether a given value meets
 * a certain condition, and returns a boolean result based on that condition.
 *
 * @param <T> the type parameter for the value that the condition is applied to.
 */
fun interface ConditionalConsumer<T> {

    /**
     * Evaluates whether the given value satisfies a certain condition.
     *
     * @param value The value to be evaluated.
     * @return `true` if the value meets the condition, `false` otherwise.
     */
    fun expect(value: T): Boolean
}
