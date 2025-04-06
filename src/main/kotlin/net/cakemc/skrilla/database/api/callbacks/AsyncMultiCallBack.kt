package net.cakemc.database.callbacks

import net.cakemc.database.cursor.Cursor

/**
 * The interface Async multi call back.
 *
 * @param <T> the type parameter
</T> */
interface AsyncMultiCallBack<T> {
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
     * @param state     the state
     * @param documents the documents
     * @param exception the exception
     */
    fun accept(state: AsyncCallBack.State?, documents: Cursor<T>?, exception: Exception?)

    /**
     * Accept not found.
     */
    fun acceptNotFound() {
        this.accept(AsyncCallBack.State.NOT_FOUND, null, IllegalArgumentException("piece not found!"))
    }

    /**
     * Accept found.
     *
     * @param documents the documents
     */
    fun acceptFound(documents: Cursor<T>) {

        this.accept(AsyncCallBack.State.FOUND, documents, null)
    }

    /**
     * Accept exception.
     *
     * @param exception the exception
     */
    fun acceptException(exception: Exception?) {
        this.accept(AsyncCallBack.State.ERROR, null, exception)
    }
}
