package net.cakemc.skrilla.database.exceptions

import java.io.IOException

/**
 * Exception thrown when the database fails to open.
 *
 * @param e The underlying IOException that caused the failure.
 */
class DatabaseOpenFailed(e: IOException?) : DatabaseException(e)
