package net.cakemc.skrilla.database.segment

import net.cakemc.skrilla.database.lookup.LookupIterator
import java.io.IOException

/**
 * A segment wrapper that represents a collection of underlying segments.
 *
 * Provides read-only access across multiple segments in reverse chronological order.
 * Operations that mutate data are unsupported and will throw exceptions.
 *
 * @param segments the list of segments, ordered from oldest to newest
 */
class MultiSegment(private val segments: List<Segment?>) : Segment {

    /**
     * Unsupported operation. Always throws [IllegalStateException].
     *
     * @throws IOException always
     */
    @Throws(IOException::class)
    override fun put(key: ByteArray, value: ByteArray): ByteArray? {
        throwIllegalState("Put called on MultiSegmentIterator")
    }

    /**
     * Returns the first non-null value found for the specified [key] when searched
     * in reverse (newest to oldest) order.
     *
     * @return the value for the given key, or null if not found
     * @throws IOException if an error occurs during lookup
     */
    @Throws(IOException::class)
    override operator fun get(key: ByteArray): ByteArray? {
        for (segment in segments.asReversed()) {
            segment?.get(key)?.let { return it }
        }
        return null
    }

    /**
     * Returns the total size of all segments combined.
     *
     * @return the total size in bytes
     */
    override fun size(): Long {
        return segments.sumOf { it?.size() ?: 0 }
    }

    /**
     * Unsupported operation. Always throws [IllegalStateException].
     *
     * @throws IOException always
     */
    @Throws(IOException::class)
    override fun remove(key: ByteArray): ByteArray? {
        throwIllegalState("Remove called on MultiSegmentIterator")
    }

    /**
     * Unsupported operation. Always throws [IllegalStateException].
     *
     * @throws IOException always
     */
    @Throws(IOException::class)
    override fun close() {
        throwIllegalState("close() should not be called on a multi-segment")
    }

    /**
     * Unsupported operation. Always throws [IllegalStateException].
     *
     * @throws IOException always
     */
    @Throws(IOException::class)
    override fun removeSegment() {
        throwIllegalState("removeSegment() should not be called on a multi-segment")
    }

    /**
     * Unsupported operation. Always throws [IllegalStateException].
     */
    override fun removeOnFinalize() {
        throwIllegalState("removeOnFinalize() should not be called on a multi-segment")
    }

    /**
     * Returns an empty set, as multi-segment does not manage files directly.
     *
     * @return an empty [Set] of file names
     */
    override fun files(): Collection<String> {
        return emptySet()
    }

    /**
     * Returns a [LookupIterator] that aggregates results from all underlying segments.
     *
     * @param lower optional lower key bound
     * @param upper optional upper key bound
     * @return a composite iterator across all segments
     * @throws IOException if an error occurs during lookup
     */
    @Throws(IOException::class)
    override fun lookup(lower: ByteArray?, upper: ByteArray?): LookupIterator {
        val iterators = segments.mapNotNull { it?.lookup(lower, upper) }
        return net.cakemc.skrilla.database.segment.MultiSegmentIterator(iterators)
    }

    /**
     * Unsupported operation. Always throws [RuntimeException].
     *
     * @throws RuntimeException always
     */
    override fun lowerID(): Long {
        throw RuntimeException("MultiSegment does not implement lowerID()")
    }

    /**
     * Unsupported operation. Always throws [RuntimeException].
     *
     * @throws RuntimeException always
     */
    override fun upperID(): Long {
        throw RuntimeException("MultiSegment does not implement upperID()")
    }

    /**
     * Helper method to throw an [IllegalStateException] with a custom message.
     *
     * @param message the exception message
     * @return nothing; always throws
     */
    private fun throwIllegalState(message: String): Nothing {
        throw IllegalStateException(message)
    }
}
