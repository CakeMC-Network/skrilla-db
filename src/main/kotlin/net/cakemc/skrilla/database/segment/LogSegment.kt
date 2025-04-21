/**
 * Represents a read-only log segment in a key-value storage engine.
 *
 * This segment loads its key-value pairs from an append-only log file,
 * and it supports lookup operations over an in-memory sorted map.
 * Mutation operations are explicitly disallowed to ensure immutability.
 */

package net.cakemc.skrilla.database.segment

import net.cakemc.skrilla.database.imdb.Options
import net.cakemc.skrilla.database.imdb.Removable
import net.cakemc.skrilla.database.imdb.WriteBatch
import net.cakemc.skrilla.database.io.Deleter
import net.cakemc.skrilla.database.io.LogFile
import net.cakemc.skrilla.database.lookup.LookupIterator
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentSkipListMap

/**
 * LogSegment is a read-only implementation of [Segment] that loads
 * its key-value data from a log file on disk.
 *
 * Represents a read-only log segment in a key-value storage engine.
 *
 * This segment loads its key-value pairs from an append-only log file,
 * and it supports lookup operations over an in-memory sorted map.
 * Mutation operations are explicitly disallowed to ensure immutability.
 *
 * @property filePath the path to the log file
 * @property options configuration options for reading the log file
 */
class LogSegment(
    private val filePath: String,
    private val options: Options
) : Segment {

    /** Path object representing the file location */
    private val path: Path = Path.of(filePath)

    /** Size of the file in bytes */
    private val segmentSize: Long = Files.size(path)

    /** In-memory map containing key-value pairs loaded from the log file */
    private val keyValueMap: ConcurrentSkipListMap<ByteArray, ByteArray> = LogFile.readLogFile(filePath, options)

    /** Segment ID extracted from the file name */
    private val segmentId: Long = getSegmentId(filePath)

    /**
     * Returns the size of the segment in bytes.
     *
     * @return size of the segment
     */
    override fun size(): Long = segmentSize

    /**
     * Returns the lower ID of this segment (same as upper ID for log segments).
     *
     * @return segment ID
     */
    override fun lowerID(): Long = segmentId

    /**
     * Returns the upper ID of this segment (same as lower ID for log segments).
     *
     * @return segment ID
     */
    override fun upperID(): Long = segmentId

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the key to look up
     * @return the value if found, null otherwise
     */
    override fun get(key: ByteArray): ByteArray? = keyValueMap[key]

    /**
     * Disallows writing to this immutable segment.
     *
     * @throws IllegalStateException always
     */
    override fun put(key: ByteArray, value: ByteArray): ByteArray? {
        throw IllegalStateException("put() called on immutable segment")
    }

    /**
     * Disallows writing a batch to this immutable segment.
     *
     * @throws IllegalStateException always
     */
    fun write(batch: WriteBatch?) {
        throw IllegalStateException("write() called on immutable segment")
    }

    /**
     * Disallows removal from this immutable segment.
     *
     * @throws IllegalStateException always
     */
    override fun remove(key: ByteArray): ByteArray? {
        throw IllegalStateException("remove() called on immutable segment")
    }

    /**
     * Closes the segment. No resources need to be closed for a log segment.
     */
    override fun close() {
        // No resources to close
    }

    /**
     * Deletes the underlying segment file from disk.
     */
    override fun removeSegment() {
        Files.delete(path)
    }

    /**
     * Returns an iterator to lookup keys within the optional range.
     *
     * @param lower optional lower bound key
     * @param upper optional upper bound key
     * @return a [LookupIterator] over the in-memory key-value map
     */
    override fun lookup(lower: ByteArray?, upper: ByteArray?): LookupIterator {
        return MemorySegment.Companion.getLookupIterator(lower, upper, keyValueMap)
    }

    /**
     * Registers this segment to be deleted when finalized.
     */
    override fun removeOnFinalize() {
        Deleter.removeOnFinalize(this, createRemovable(path))
    }

    /**
     * Returns the file names associated with this segment.
     *
     * @return a collection containing the file name
     */
    override fun files(): Collection<String> {
        return setOf(path.fileName.toString())
    }

    /**
     * Companion object for utility methods.
     */
    companion object {

        /**
         * Creates a [Removable] wrapper for the given file path.
         *
         * @param path the file path to wrap
         * @return a [Removable] implementation
         */
        private fun createRemovable(path: Path): Removable {
            return object : Removable {
                override fun remove() {
                    Files.delete(path)
                }

                override fun toString(): String = "LogSegment:$path"
            }
        }
    }

    /**
     * Extracts the segment ID from the log file name.
     *
     * @param path the file name (expects format with ID after first dot)
     * @return the parsed segment ID
     * @throws IllegalArgumentException if ID cannot be parsed
     */
    fun getSegmentId(path: String): Long =
        path.split(".").getOrNull(1)?.toLongOrNull() ?: throw IllegalArgumentException("Invalid segment ID in $path")
}