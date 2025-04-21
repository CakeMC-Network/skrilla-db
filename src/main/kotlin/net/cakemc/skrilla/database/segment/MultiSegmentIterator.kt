package net.cakemc.skrilla.database.segment

import net.cakemc.skrilla.database.imdb.KeyValue
import net.cakemc.skrilla.database.lookup.LookupIterator
import java.io.IOException
import java.util.*

/**
 * A [LookupIterator] that merges results from multiple underlying iterators,
 * providing a unified, deduplicated, sorted view across all segments.
 *
 * Iterators are assumed to be ordered from oldest to newest. Newer segments take precedence
 * when duplicate keys exist.
 *
 * @param iterators list of iterators ordered from oldest to newest
 */
class MultiSegmentIterator(private val iterators: List<LookupIterator?>) : LookupIterator {

    /**
     * Unsupported operation. [peekKey] is not available for merged iterators.
     *
     * @throws IllegalStateException always
     */
    @Throws(IOException::class)
    override fun peekKey(): ByteArray? {
        throw IllegalStateException("peekKey called on MultiSegmentIterator")
    }

    /**
     * Returns the next deduplicated and ordered [KeyValue] from the combined iterators.
     * Newer segments override older entries with the same key.
     *
     * @return the next unique [KeyValue], or null if all iterators are exhausted
     * @throws IOException if an underlying iterator fails
     */
    @Throws(IOException::class)
    override fun next(): KeyValue? {
        var currentIndex = -1
        var lowest: ByteArray? = null

        for (i in iterators.indices.reversed()) {
            val iterator = iterators[i] ?: continue
            val key = iterator.peekKey() ?: continue
            if (lowest == null || Arrays.compare(key, lowest) < 0) {
                lowest = key
                currentIndex = i
            }
        }

        if (currentIndex == -1) {
            return null
        }

        val kv = iterators[currentIndex]!!.next()

        for (i in iterators.indices.reversed()) {
            if (i == currentIndex) continue
            val iterator = iterators[i] ?: continue

            while (true) {
                val key = iterator.peekKey() ?: break
                if (Arrays.compare(key, lowest) <= 0) {
                    iterator.next()
                } else {
                    break
                }
            }
        }

        return kv
    }
}
