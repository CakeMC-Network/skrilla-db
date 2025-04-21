package net.cakemc.database.callbacks

import net.cakemc.database.api.Document

/**
 * The interface for handling asynchronous callback operations that return a single `Document` object.
 * This interface extends `AsyncCallBack<Document?>`, allowing for custom handling of a single `Document` object.
 *
 * It is typically used when querying a single document asynchronously and provides a callback method
 * that accepts the result once the query completes. If no document is found, the result will be `null`.
 *
 * The callback will handle both success (where a `Document` is found) and failure scenarios (e.g., an exception occurs).
 */
fun interface DocumentAsyncCallBack : AsyncCallBack<Document?> {

    /**
     * Handles the exception if an error occurs during the asynchronous operation.
     *
     * @param exception The exception that was thrown.
     */
    override fun acceptException(exception: Exception) {
        super.acceptException(exception)
    }
}
