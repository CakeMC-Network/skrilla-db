package net.cakemc.skrilla.database

import net.cakemc.skrilla.database.exceptions.DatabaseException
import net.cakemc.skrilla.database.imdb.Snapshot
import net.cakemc.skrilla.database.imdb.Statistics
import java.io.IOException

/**
 * An abstract base class representing an in-memory key-value database.
 *
 * This class defines the fundamental operations supported by an in-memory database,
 * including inserting, retrieving, and deleting keys, as well as obtaining snapshots
 * and statistics. Subclasses must provide concrete implementations of these operations.
 */
abstract class AbstractMemoryDatabase {

    /**
     * Inserts or updates the value associated with the specified key.
     *
     * @param key the key to store, must not be null.
     * @param value the value to associate with the key, must not be null.
     * @throws IOException if an I/O error occurs during the operation.
     */
    @Throws(IOException::class)
    abstract fun put(key: ByteArray, value: ByteArray)

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key to retrieve.
     * @return the value associated with the key, or `null` if the key does not exist.
     * @throws IOException if an I/O error occurs during the operation.
     */
    @Throws(IOException::class)
    abstract fun get(key: ByteArray): ByteArray?

    /**
     * Removes the entry associated with the specified key.
     *
     * @param key the key to remove.
     * @return the value previously associated with the key, or `null` if the key was not present.
     * @throws IOException if an I/O error occurs during the operation.
     */
    @Throws(IOException::class)
    abstract fun remove(key: ByteArray): ByteArray?

    /**
     * Creates a consistent snapshot of the current state of the database.
     *
     * @return a [Snapshot] representing the current state of the database.
     * @throws IOException if an I/O error occurs while creating the snapshot.
     */
    @Throws(IOException::class)
    abstract fun snapshot(): Snapshot

    /**
     * Returns internal performance or usage statistics for the database.
     *
     * @return a [Statistics] object containing metrics such as memory usage, record count, etc.
     */
    abstract fun stats(): Statistics

    /**
     * Closes the database and releases all resources.
     *
     * After calling this method, the database instance should no longer be used.
     *
     * @throws DatabaseException if the database encounters an internal failure during closure.
     * @throws IOException if an I/O error occurs while closing the database.
     */
    @Throws(DatabaseException::class, IOException::class)
    abstract fun close()
}
