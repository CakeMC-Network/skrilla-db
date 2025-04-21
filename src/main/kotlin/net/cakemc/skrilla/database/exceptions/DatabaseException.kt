package net.cakemc.skrilla.database.exceptions

/**
 * Base class for all exceptions related to the database.
 * This class serves as the parent for all database-related exceptions and allows passing error messages or underlying exceptions.
 */
open class DatabaseException : Exception {

    /**
     * Default constructor for the exception.
     */
    protected constructor() : super()

    /**
     * Constructor that allows passing another exception as the cause.
     *
     * @param e The exception that caused this exception to be thrown.
     */
    protected constructor(e: Exception?) : super(e)

    /**
     * Constructor that allows passing a custom error message.
     *
     * @param msg The error message describing the exception.
     */
    constructor(msg: String?) : super(msg)
}
