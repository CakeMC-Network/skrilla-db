package net.cakemc.skrilla.database.segment

import net.cakemc.skrilla.database.lookup.LookupIterator
import java.io.IOException

/**
 * Represents a read-only or mutable storage unit within the segment architecture.
 * Implementations may represent in-memory, file-based, or compound segments.
 */
interface Segment {

    /**
     * Returns the highest ID this segment covers.
     */
    fun upperID(): Long

    /**
     * Returns the lowest ID this segment covers.
     */
    fun lowerID(): Long

    /**
     * Adds or updates the given [key] with [value].
     * May throw if the segment is immutable.
     *
     * @return the previous value if it existed, or null
     * @throws IOException if the operation fails
     */
    @Throws(IOException::class)
    fun put(key: ByteArray, value: ByteArray): ByteArray?

    /**
     * Retrieves the value associated with [key], or null if not found.
     *
     * @throws IOException if the operation fails
     */
    @Throws(IOException::class)
    operator fun get(key: ByteArray): ByteArray?

    /**
     * Removes the entry associated with [key].
     * May throw if the segment is immutable.
     *
     * @return the previous value if it existed, or null
     * @throws IOException if the operation fails
     */
    @Throws(IOException::class)
    fun remove(key: ByteArray): ByteArray?

    /**
     * Closes any open resources for this segment.
     *
     * @throws IOException if closing fails
     */
    @Throws(IOException::class)
    fun close()

    /**
     * Returns an iterator over the range from [lower] to [upper] inclusive.
     *
     * @throws IOException if iterator creation fails
     */
    @Throws(IOException::class)
    fun lookup(lower: ByteArray?, upper: ByteArray?): LookupIterator?

    /**
     * Deletes persistent storage backing this segment, if applicable.
     *
     * @throws IOException if deletion fails
     */
    @Throws(IOException::class)
    fun removeSegment()

    /**
     * Marks this segment for deferred deletion on finalization (e.g., GC cleanup).
     */
    fun removeOnFinalize()

    /**
     * Returns a collection of file names that back this segment.
     */
    fun files(): Collection<String>

    /**
     * Returns the total size of the segment in bytes.
     */
    fun size(): Long

    companion object {
        /**
         * Creates a shallow copy of [list] with [segment] appended to the end.
         */
        fun copyAndAppend(list: List<Segment?>, segment: Segment?): List<Segment?> {
            val copy: MutableList<Segment?> = ArrayList(list.size + 1)
            copy.addAll(list)
            copy.add(segment)
            return copy
        }
    }
}
