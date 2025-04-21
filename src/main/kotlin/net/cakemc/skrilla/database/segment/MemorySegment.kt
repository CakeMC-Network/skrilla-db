package net.cakemc.skrilla.database.segment

import net.cakemc.skrilla.database.imdb.*
import net.cakemc.skrilla.database.io.Deleter
import net.cakemc.skrilla.database.io.LogFile
import net.cakemc.skrilla.database.lookup.LookupIterator
import java.io.IOException
import java.util.concurrent.ConcurrentSkipListMap

/**
 * Represents an in-memory segment of key-value pairs, optionally backed by a log file
 * for durability. This segment is mutable until finalized.
 *
 * Provides operations for insertion, deletion, lookup, and iteration,
 * and supports batch write operations.
 *
 * @param path the path to the backing log file (if any)
 * @param id the unique identifier for the segment
 * @param options the database options used for key comparison
 */
class MemorySegment(
    private val path: String?,
    private val id: Long,
    private val options: Options
) : Segment {

    /** In-memory map of keys to values */
    private val keyValueMap = ConcurrentSkipListMap<ByteArray, ByteArray>(
        KeyComparison.newKeyCompare(options)
    )

    /** Optional log file for persistence */
    private var logFile: LogFile? = null

    /** Total size in bytes of the keys and values in the segment */
    private var totalSizeBytes = 0

    /**
     * Returns the upper segment ID, which is equal to the lower ID in this implementation.
     */
    override fun upperID(): Long = id

    /**
     * Returns the lower segment ID, which is equal to the upper ID in this implementation.
     */
    override fun lowerID(): Long = id

    /**
     * Returns the total size in bytes of all key-value pairs in this segment.
     */
    override fun size(): Long = totalSizeBytes.toLong()

    /**
     * Writes a single key-value pair to the segment.
     *
     * @param key the key to write
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or null
     * @throws IOException if an error occurs while writing to the log
     */
    @Throws(IOException::class)
    override fun put(key: ByteArray, value: ByteArray): ByteArray? {
        maybeCreateLogFile()
        val previousValue = keyValueMap.put(key, value)
        totalSizeBytes += key.size + value.size - (previousValue?.let { key.size + it.size } ?: 0)
        logFile?.write(key, value)
        return previousValue
    }

    /**
     * Retrieves the value for the given key.
     *
     * @param key the key to look up
     * @return the value, or null if not found or marked as deleted
     * @throws IOException if an error occurs
     */
    @Throws(IOException::class)
    override fun get(key: ByteArray): ByteArray? {
        val value = keyValueMap[key]
        return if (value != null && value.isEmpty()) null else value
    }

    /**
     * Marks a key as deleted by associating it with an empty value.
     *
     * @param key the key to delete
     * @return the previous value associated with the key
     * @throws IOException if an error occurs
     */
    @Throws(IOException::class)
    override fun remove(key: ByteArray): ByteArray? {
        return put(key, KeyValue.EMPTY)
    }

    /**
     * Writes a batch of key-value entries into the segment.
     *
     * @param batch the batch of entries to write
     * @throws IOException if an error occurs during the batch write
     */
    @Throws(IOException::class)
    fun write(batch: WriteBatch) {
        maybeCreateLogFile()
        logFile?.startBatch(batch.entries.size)
        for (entry in batch.entries) {
            val previousValue = keyValueMap.put(entry.key, entry.value)
            totalSizeBytes += entry.key.size + entry.value.size - (previousValue?.let { entry.key.size + it.size } ?: 0)
            logFile?.write(entry.key, entry.value)
        }
        logFile?.endBatch(batch.entries.size)
    }

    /**
     * Closes this segment. Currently a no-op.
     */
    @Throws(IOException::class)
    override fun close() {}

    /**
     * Returns the filenames associated with this segment.
     *
     * @return a collection containing the log file name, or empty if none
     */
    override fun files(): Collection<String> =
        logFile?.let { setOf(it.filepath.fileName.toString()) } ?: emptySet()

    /**
     * Returns an iterator over keys within the given bounds.
     *
     * @param lower the lower bound key, or null
     * @param upper the upper bound key, or null
     * @return an iterator over matching key-value entries
     * @throws IOException if an error occurs
     */
    @Throws(IOException::class)
    override fun lookup(lower: ByteArray?, upper: ByteArray?): LookupIterator {
        return getLookupIterator(lower, upper, keyValueMap)
    }

    /**
     * Marks the segment for removal on finalization.
     */
    override fun removeOnFinalize() {
        Deleter.removeOnFinalize(this, createRemovable(this))
    }

    /**
     * Removes this segment's resources (e.g., deletes log file).
     *
     * @throws IOException if an error occurs during removal
     */
    @Throws(IOException::class)
    override fun removeSegment() {
        logFile?.remove()
    }

    /**
     * Creates the log file if it hasn't been created yet.
     */
    @Throws(IOException::class)
    private fun maybeCreateLogFile() {
        if (logFile != null || path.isNullOrEmpty()) return
        logFile = LogFile(path, id, options)
    }

    companion object {

        /**
         * Creates an in-memory segment with no backing file and default options.
         *
         * @return a new memory-only segment
         */
        fun newMemoryOnlySegment(): MemorySegment = MemorySegment("", 0, Options())

        /**
         * Returns a LookupIterator over a specified range of the given map.
         *
         * @param lower the lower key bound
         * @param upper the upper key bound
         * @param list the map to iterate over
         * @return a range-aware LookupIterator
         */
        fun getLookupIterator(
            lower: ByteArray?,
            upper: ByteArray?,
            list: ConcurrentSkipListMap<ByteArray, ByteArray>
        ): LookupIterator {
            return when {
                lower == null && upper == null -> net.cakemc.skrilla.database.segment.MemorySegmentIterator(list)
                lower == null -> net.cakemc.skrilla.database.segment.MemorySegmentIterator(list.headMap(upper, true))
                upper == null -> net.cakemc.skrilla.database.segment.MemorySegmentIterator(list.tailMap(lower, true))
                else -> net.cakemc.skrilla.database.segment.MemorySegmentIterator(list.subMap(lower, true, upper, true))
            }
        }

        /**
         * Wraps a MemorySegment's log file in a Removable interface for cleanup.
         *
         * @param memorySegment the segment to make removable
         * @return a removable wrapper
         */
        private fun createRemovable(memorySegment: MemorySegment): Removable {
            val logRef = memorySegment.logFile
            return object : Removable {
                @Throws(IOException::class)
                override fun remove() {
                    logRef?.remove()
                }

                override fun toString(): String {
                    return "LogFile:${logRef?.filepath}"
                }
            }
        }
    }
}
