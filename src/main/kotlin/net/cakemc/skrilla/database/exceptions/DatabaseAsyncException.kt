package net.cakemc.skrilla.database.exceptions

/**
 * Exception thrown when an asynchronous database operation fails.
 * This exception is used to indicate that an error occurred during an asynchronous operation.
 *
 * @param error The underlying exception that caused the asynchronous operation to fail.
 */
class DatabaseAsyncException(error: Exception?) : DatabaseException(error)
