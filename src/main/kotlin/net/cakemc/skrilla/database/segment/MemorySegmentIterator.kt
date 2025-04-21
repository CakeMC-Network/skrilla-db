package net.cakemc.skrilla.database.segment

import net.cakemc.skrilla.database.imdb.KeyValue
import net.cakemc.skrilla.database.lookup.LookupIterator
import java.io.IOException
import java.util.*

/**
 * Iterator implementation for traversing a sorted map of key-value pairs
 * used in memory segments.
 *
 * Supports peeking the next key without advancing and returning wrapped
 * key-value pairs.
 *
 * @param map the sorted map to iterate over
 */
internal class MemorySegmentIterator(map: SortedMap<ByteArray?, ByteArray?>) : LookupIterator {

    private val entryIterator = map.entries.iterator()
    private var cachedNext: Map.Entry<ByteArray?, ByteArray?>? = null

    /**
     * Returns the next key in the iteration without advancing the iterator.
     *
     * @return the next key or null if the end is reached
     * @throws IOException if an error occurs
     */
    @Throws(IOException::class)
    override fun peekKey(): ByteArray? {
        if (cachedNext == null && entryIterator.hasNext()) {
            cachedNext = entryIterator.next()
        }
        return cachedNext?.key
    }

    /**
     * Returns the next key-value pair wrapped in a [KeyValue] object.
     *
     * @return the next key-value pair, or null if none remain
     * @throws IOException if an error occurs
     */
    @Throws(IOException::class)
    override fun next(): KeyValue? {
        if (cachedNext == null && entryIterator.hasNext()) {
            cachedNext = entryIterator.next()
        }
        return cachedNext?.let {
            try {
                if (it.key != null && it.value != null)
                    KeyValue(it.key!!, it.value!!)
                else
                    null
            } finally {
                cachedNext = null
            }
        }
    }

    /**
     * Returns the next entry in the iterator without consuming it.
     *
     * @return the next [Map.Entry] or null if none remain
     */
    fun advance(): Map.Entry<ByteArray?, ByteArray?>? {
        if (cachedNext == null && entryIterator.hasNext()) {
            cachedNext = entryIterator.next()
        }
        return cachedNext
    }
}
