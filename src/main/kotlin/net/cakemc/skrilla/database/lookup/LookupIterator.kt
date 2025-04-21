package net.cakemc.skrilla.database.lookup

import net.cakemc.skrilla.database.imdb.KeyValue
import java.io.IOException

/**
 * Interface for iterating over key-value pairs during a lookup operation.
 * This interface provides functionality to retrieve the next key-value pair in the sequence.
 */
interface LookupIterator : LookupIteratorInternal {

    /**
     * Returns the next [KeyValue] in the sequence.
     *
     * This method retrieves the next available key-value pair in the iteration. If there are no more elements to iterate over, it returns `null`.
     * If the value has been logically removed, [KeyValue.value] will be [KeyValue.EMPTY].
     *
     * @return The next [KeyValue] or `null` if no more elements exist.
     * @throws IOException If an error occurs while reading the data.
     */
    @Throws(IOException::class)
    fun next(): KeyValue?
}
