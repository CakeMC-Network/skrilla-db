package net.cakemc.skrilla.database.imdb

import net.cakemc.skrilla.database.InMemoryDatabase
import net.cakemc.skrilla.database.exceptions.DatabaseClosedException
import net.cakemc.skrilla.database.lookup.LookupIterator
import java.io.IOException

/**
 * The DatabaseLookup class provides an iterator over key-value pairs within a database.
 * It wraps a given LookupIterator and ensures the database is open while filtering out entries
 * with empty values.
 *
 * @param itr The LookupIterator instance to iterate over the key-value pairs.
 * @param database The InMemoryDatabase instance to check for open state.
 */
internal class DatabaseLookup(
    private val itr: LookupIterator, // The iterator that provides the key-value pairs
    private val database: InMemoryDatabase // The in-memory database instance
) : LookupIterator {

    /**
     * This method is unsupported for the DatabaseLookup iterator.
     * It throws an IllegalStateException if called, as peekKey() should not be used in this context.
     *
     * @throws IllegalStateException Always thrown to indicate that peekKey should not be called.
     */
    @Throws(IOException::class)
    override fun peekKey(): ByteArray? {
        // This operation is not supported, as the behavior of peekKey is not implemented in this iterator.
        throw IllegalStateException("peekKey should not be called")
    }

    /**
     * This method retrieves the next valid key-value pair from the iterator.
     * It ensures that the database is open before proceeding and will skip any
     * entries that have an empty value. If the iterator reaches the end of the
     * available entries or the database is closed, an exception will be thrown.
     *
     * @return A KeyValue object containing the next key-value pair, or null if no more valid entries exist.
     * @throws DatabaseClosedException If the database is closed while attempting to retrieve the next key-value pair.
     * @throws IOException If there is an input/output issue during the operation.
     */
    @Throws(IOException::class)
    override fun next(): KeyValue? {
        while (true) {
            // Check if the database is still open before proceeding.
            if (!database.open) {
                throw DatabaseClosedException() // Database is closed, so throw an exception.
            }

            // Retrieve the next key-value pair from the iterator.
            val kv = itr.next()

            // If the key-value pair is valid and has a non-empty value, return it.
            if (kv != null && kv.value.size > 0) {
                return kv
            }
        }
    }
}
