package net.cakemc.database.callbacks

import net.cakemc.database.cursor.Cursor

/**
 * A callback interface used for handling the result of asynchronous operations
 * that return multiple items of type [T]. It supports handling different states
 * such as found, not found, and error.
 *
 * @param <T> the type parameter of the elements in the cursor
 */
interface AsyncMultiCallBack<T> {

    /**
     * The enum representing the different states of the callback.
     */
    enum class State {
        /**
         * The state indicating that the requested documents were found.
         */
        FOUND,

        /**
         * The state indicating that no documents were found matching the criteria.
         */
        NOT_FOUND,

        /**
         * The state indicating that an error occurred during the operation.
         */
        ERROR,
    }

    /**
     * Accepts the result of the asynchronous operation, including its state and any associated data.
     *
     * @param state The state of the operation (e.g., [FOUND], [NOT_FOUND], [ERROR]).
     * @param documents The cursor containing the resulting documents, or null if not applicable.
     * @param exception The exception encountered during the operation, or null if none occurred.
     */
    fun accept(state: State?, documents: Cursor<T>?, exception: Exception?)

    /**
     * Accepts a "not found" state when no matching documents were found.
     *
     * This method is a convenience method for passing the `NOT_FOUND` state.
     */
    fun acceptNotFound() {
        this.accept(State.NOT_FOUND, null, IllegalArgumentException("piece not found!"))
    }

    /**
     * Accepts a "found" state with the given cursor of documents.
     *
     * @param documents The cursor containing the found documents.
     */
    fun acceptFound(documents: Cursor<T>) {
        this.accept(State.FOUND, documents, null)
    }

    /**
     * Accepts an exception state when an error occurred during the operation.
     *
     * @param exception The exception that occurred.
     */
    fun acceptException(exception: Exception?) {
        this.accept(State.ERROR, null, exception)
    }
}
