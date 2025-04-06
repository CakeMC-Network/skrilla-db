package net.cakemc.database.callbacks

/**
 * The interface Database listener.
 */
fun interface DatabaseListener {
    /**
     * The enum State.
     */
    enum class State {
        /**
         * Success state.
         */
        SUCCESS,

        /**
         * Failed state.
         */
        FAILED,
    }

    /**
     * Accept.
     *
     * @param state     the state
     * @param exception the exception
     */
    fun accept(state: State, exception: Exception?)

    /**
     * Exception.
     *
     * @param exception the exception
     */
    fun exception(exception: Exception) {
        this.accept(State.FAILED, exception)
    }

    /**
     * Success.
     */
    fun success() {
        this.accept(State.SUCCESS, null)
    }
}
