package net.cakemc.skrilla.database.exceptions

/**
 * Exception thrown when the database is currently in use.
 * This exception is used to indicate that an operation cannot proceed because the database is already in use.
 */
class DatabaseInUseException : DatabaseException()
