package net.cakemc.database.api

/**
 * Represents an abstract base class for database records, containing the common properties
 * shared across different types of records. This class provides the basic structure to store
 * the index, flags, and id, as well as a method to retrieve the size of the record.
 *
 * Each concrete subclass (e.g., [Document]) should implement the [size] method to return the
 * specific size of the record, based on its own data and structure.
 *
 * @property index The index of the database record.
 * @property flags The flags associated with the record, represented as an integer.
 * @property id The unique identifier for the record.
 *
 * @constructor Creates a new database record with the given index, flags, and ID.
 */
abstract class DatabaseRecord protected constructor(
    /**
     * The index of the database record.
     */
    val index: Int,

    /**
     * The flags associated with the database record.
     */
    var flags: Int,

    /**
     * The unique identifier for the database record.
     */
    val id: Long
) {

    /**
     * Abstract method to retrieve the size of the record.
     * The size may vary based on the specific subclass (e.g., [Document])
     * that implements this method.
     *
     * @return The size of the record.
     */
    abstract fun size(): Int
}
