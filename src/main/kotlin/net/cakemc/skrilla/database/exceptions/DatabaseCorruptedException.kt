package net.cakemc.skrilla.database.exceptions

import java.io.IOException

/**
 * Exception thrown when the database is corrupted.
 * This exception indicates that the database's internal structure or data is corrupted and cannot be recovered.
 *
 * @param e The underlying `IOException` that caused the corruption.
 */
class DatabaseCorruptedException(e: IOException?) : DatabaseException(e)
