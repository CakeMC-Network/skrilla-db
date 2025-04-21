package net.cakemc.skrilla.database.exceptions

import java.io.IOException

/**
 * Exception thrown when an operation is attempted on a closed database.
 * This exception is used to indicate that the database is closed and cannot be accessed or modified.
 */
class DatabaseClosedException : IOException()
