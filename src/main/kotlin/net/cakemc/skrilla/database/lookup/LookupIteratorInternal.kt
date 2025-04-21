package net.cakemc.skrilla.database.lookup

import java.io.IOException

/**
 * Internal interface for iterating over key-value pairs during a lookup operation.
 * This interface provides functionality to peek at the next key without advancing the iterator.
 */
interface LookupIteratorInternal {

    /**
     * Returns the next key without advancing the iterator.
     *
     * This method allows you to inspect the next key in the iteration sequence without moving the iterator forward.
     * If there are no more keys to inspect, it returns `null`.
     *
     * @return The next key as a [ByteArray], or `null` if no more keys are available.
     * @throws IOException If an error occurs while reading the key.
     */
    @Throws(IOException::class)
    fun peekKey(): ByteArray?
}
