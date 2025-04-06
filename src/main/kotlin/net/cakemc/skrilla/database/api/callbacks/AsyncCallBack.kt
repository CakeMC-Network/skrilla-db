package net.cakemc.database.callbacks

/**
 * The interface Async call back.
 *
 * @param <Document> the type parameter
</Document> */
interface AsyncCallBack<Document> {
    /**
     * The enum State.
     */
    enum class State {
        /**
         * Found state.
         */
        FOUND,

        /**
         * Not found state.
         */
        NOT_FOUND,

        /**
         * Error state.
         */
        ERROR,
    }

    /**
     * Accept.
     *
     * @param document  the document
     * @param state     the state
     * @param exception the exception
     */
    fun accept(document: Document?, state: State?, exception: Exception?)

    /**
     * Accept not found.
     */
    fun acceptNotFound() {
        this.accept(null, State.NOT_FOUND, IllegalArgumentException("piece not found!"))
    }

    /**
     * Accept found.
     *
     * @param document the document
     */
    fun acceptFound(document: Document?) {
        if (document == null) {
            this.accept(document, State.ERROR, IllegalArgumentException("piece is null but system tells its there!"))
            return
        }

        this.accept(document, State.FOUND, null)
    }

    /**
     * Accept exception.
     *
     * @param exception the exception
     */
    fun acceptException(exception: Exception) {
        this.accept(null, State.ERROR, exception)
    }
}
