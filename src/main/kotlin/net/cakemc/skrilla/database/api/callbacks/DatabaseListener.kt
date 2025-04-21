package net.cakemc.database.callbacks

/**
 * The interface for handling database operation results asynchronously.
 * It defines methods for success and failure scenarios, allowing for callback-style handling
 * of database operations such as inserts, updates, or deletions.
 *
 * Implementations of this interface are used to monitor the outcome of database operations
 * and respond accordingly based on whether the operation was successful or if an error occurred.
 */
fun interface DatabaseListener {

    /**
     * Enum representing the state of a database operation.
     */
    enum class State {
        /**
         * Indicates that the operation was successful.
         */
        SUCCESS,

        /**
         * Indicates that the operation failed.
         */
        FAILED,
    }

    /**
     * Accepts the result of a database operation, with the state of the operation
     * and any associated exception (if applicable).
     *
     * @param state The state of the operation (either `SUCCESS` or `FAILED`).
     * @param exception The exception that occurred during the operation, if any.
     *                  `null` if the operation was successful.
     */
    fun accept(state: State, exception: Exception?)

    /**
     * Handles failure by accepting the `FAILED` state and the provided exception.
     *
     * @param exception The exception that caused the failure.
     */
    fun exception(exception: Exception) {
        this.accept(State.FAILED, exception)
    }

    /**
     * Handles success by accepting the `SUCCESS` state with no exception.
     */
    fun success() {
        this.accept(State.SUCCESS, null)
    }
}
