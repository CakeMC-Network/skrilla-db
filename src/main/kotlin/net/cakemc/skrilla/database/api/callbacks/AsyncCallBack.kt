package net.cakemc.database.callbacks

/**
 * A callback interface used for handling the result of asynchronous operations
 * that return a single document of type [Document]. It supports handling different states
 * such as found, not found, and error.
 *
 * @param <Document> the type of the document being returned by the asynchronous operation
 */
interface AsyncCallBack<Document> {

    /**
     * The enum representing the different states of the callback.
     */
    enum class State {
        /**
         * The state indicating that the requested document was found.
         */
        FOUND,

        /**
         * The state indicating that no document was found matching the criteria.
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
     * @param document The document returned by the operation, or null if not applicable.
     * @param state The state of the operation (e.g., [FOUND], [NOT_FOUND], [ERROR]).
     * @param exception The exception encountered during the operation, or null if none occurred.
     */
    fun accept(document: Document?, state: State?, exception: Exception?)

    /**
     * Accepts a "not found" state when no matching document was found.
     *
     * This method is a convenience method for passing the `NOT_FOUND` state.
     */
    fun acceptNotFound() {
        this.accept(null, State.NOT_FOUND, IllegalArgumentException("piece not found!"))
    }

    /**
     * Accepts a "found" state with the given document.
     *
     * @param document The document found during the operation.
     */
    fun acceptFound(document: Document?) {
        if (document == null) {
            this.accept(document, State.ERROR, IllegalArgumentException("piece is null but system tells its there!"))
            return
        }

        this.accept(document, State.FOUND, null)
    }

    /**
     * Accepts an exception state when an error occurred during the operation.
     *
     * @param exception The exception that occurred.
     */
    fun acceptException(exception: Exception) {
        this.accept(null, State.ERROR, exception)
    }
}
