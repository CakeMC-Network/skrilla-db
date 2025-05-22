package net.cakemc.skrilla.database.segment

import net.cakemc.skrilla.database.imdb.*
import net.cakemc.skrilla.database.imdb.CompressedKey
import net.cakemc.skrilla.database.imdb.Constants
import net.cakemc.skrilla.database.imdb.KeyBuffer
import net.cakemc.skrilla.database.io.Deleter
import net.cakemc.skrilla.database.io.LittleEndianDataInputStream
import net.cakemc.skrilla.database.lookup.LookupIterator
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * DiskSegment is a segment of data stored on disk, offering methods for lookup, retrieval,
 * and removal of data, as well as management of the underlying files.
 *
 * @param keyFilename The file containing the keys for this segment.
 * @param dataFilename The file containing the data for this segment.
 * @param keyIndex An optional list of ByteArrays representing the key index for the segment.
 */
internal class DiskSegment(
    keyFilename: String,
    dataFilename: String,
    keyIndex: List<ByteArray?>?
) : Segment {

    val keyFile = MemoryMappedFile(RandomAccessFile(File(keyFilename), "r"))
    val dataFile = MemoryMappedFile(RandomAccessFile(dataFilename, "r"))
    private val keyFilename = keyFilename
    private val dataFilename = dataFilename

    val lowerId = getSegmentIDs(keyFilename)[0]
    val upperId = getSegmentIDs(keyFilename)[1]
    val keyBlocks = (keyFile.length() - 1) / Constants.keyBlockSize + 1
    val size = Files.size(Path.of(keyFilename)) + Files.size(Path.of(dataFilename))

    val keyIndex: List<ByteArray?> = keyIndex ?: loadKeyIndex(
        keyFile,
        keyBlocks
    )

    private val EMPTY: LookupIterator = object : LookupIterator {
        override fun next(): KeyValue? = null
        override fun peekKey(): ByteArray? = null
        override fun toString() = "EmptyIterator for $this@DiskSegment"
    }

    /**
     * Extracts the segment IDs from a file name (assuming it follows a specific pattern).
     *
     * @param name The name of the file.
     * @return A LongArray containing the segment IDs.
     * @throws IllegalArgumentException if the segment name does not match the expected pattern.
     */
    fun getSegmentIDs(name: String): LongArray {
        val segs = name.split(".")
        if (segs.size < 3) throw IllegalArgumentException("Invalid segment name: $name")
        return longArrayOf(segs[1].toLong(), segs[2].toLong())
    }

    /**
     * Returns a string representation of the DiskSegment.
     *
     * @return A string representing the DiskSegment.
     */
    override fun toString(): String {
        val keyRange = if (keyIndex.isNotEmpty()) {
            "${String(keyIndex[0]!!)}<>${String(keyIndex.last()!!)}"
        } else ""
        return "DiskSegment:$keyFilename,$dataFilename:$keyRange"
    }

    /**
     * Returns the size of the segment in bytes.
     *
     * @return The size of the segment.
     */
    override fun size(): Long = size

    /**
     * Puts a key-value pair into the segment, but disk segments are immutable.
     *
     * @param key The key to be inserted.
     * @param value The value associated with the key.
     * @return Throws IllegalStateException as disk segments are immutable.
     * @throws IllegalStateException if attempting to modify a disk segment.
     */
    override fun put(key: ByteArray, value: ByteArray): ByteArray? {
        throw IllegalStateException("disk segments are immutable")
    }

    /**
     * Gets the value associated with a given key.
     *
     * @param key The key to be looked up.
     * @return The value associated with the key, or null if the key does not exist.
     */
    override fun get(key: ByteArray): ByteArray? {
        val ol = binarySearch(key) ?: return null
        return ByteArray(ol.len).also { dataFile.readAt(it, ol.offset) }
    }

    /**
     * Removes a key-value pair from the segment, but disk segments are immutable.
     *
     * @param key The key to be removed.
     * @return Throws IllegalStateException as disk segments are immutable.
     * @throws IllegalStateException if attempting to modify a disk segment.
     */
    override fun remove(key: ByteArray): ByteArray? {
        throw IllegalStateException("disk segments are immutable")
    }

    /**
     * Closes the disk segment, releasing any resources held.
     */
    override fun close() {
        keyFile.close()
        dataFile.close()
    }

    /**
     * Removes the segment by closing the files and deleting the underlying files from the filesystem.
     */
    override fun removeSegment() {
        close()
        Files.delete(Path.of(keyFilename))
        Files.delete(Path.of(dataFilename))
    }

    /**
     * Looks up a range of keys within the segment.
     *
     * @param lower The lower bound key for the lookup, or null if no lower bound.
     * @param upper The upper bound key for the lookup, or null if no upper bound.
     * @return A LookupIterator for the keys in the specified range.
     */
    override fun lookup(lower: ByteArray?, upper: ByteArray?): LookupIterator? {
        if (size == 0L) return EMPTY

        val ctx = ScanContext()
        var block = 0L
        lower?.let {
            val lh = indexSearch(it) ?: return EMPTY
            val startBlock = binarySearch0(lh.low, lh.high, it, ctx)
            if (startBlock < 0) return null
            block = startBlock
        }

        keyFile.readAt(ctx.buffer, block * Constants.keyBlockSize)
        return DiskSegmentIterator(this, lower, upper, ctx.buffer, block)
    }

    /**
     * Registers the segment to be removed on finalization (i.e., when the object is garbage collected).
     */
    override fun removeOnFinalize() {
        Deleter.removeOnFinalize(this, DiskSegment.createRemovable(this))
    }

    /**
     * Returns a collection of the file names associated with the segment.
     *
     * @return A collection containing the file names for the segment.
     */
    override fun files(): Collection<String> =
        listOf(getFileName(keyFilename), getFileName(dataFilename))

    /**
     * Extracts the file name from a given file path.
     *
     * @param filepath The full file path.
     * @return The file name extracted from the path.
     */
    fun getFileName(filepath: String): String = Path.of(filepath).fileName.toString()

    private fun binarySearch(key: ByteArray): DiskSegment.OffsetLen? {
        val ctx = DiskSegment.Companion.bufferCache.get().apply { reset() }
        val lh = indexSearch(key) ?: return null
        val block = binarySearch0(lh.low, lh.high, key, ctx)
        return scanBlock(block, key, ctx)
    }

    fun indexSearch(key: ByteArray?): LowHigh {
        if (keyIndex.isEmpty()) return LowHigh(0, keyBlocks - 1)

        var index = Collections.binarySearch(keyIndex, key) { o1, o2 -> Arrays.compare(o1, o2) }
        val lowBlock: Long
        var highBlock: Long

        if (index >= 0) {
            lowBlock = index.toLong() * Constants.keyIndexInterval
            highBlock = lowBlock
        } else {
            index = (-(index + 1) - 1).coerceAtLeast(0)
            lowBlock = index.toLong() * Constants.keyIndexInterval
            highBlock = (lowBlock + Constants.keyIndexInterval).coerceAtMost(keyBlocks - 1)
        }

        return LowHigh(lowBlock, highBlock)
    }

    fun binarySearch0(lowBlock: Long, highBlock: Long, key: ByteArray, ctx: ScanContext): Long {
        if (highBlock - lowBlock <= 1) {
            keyFile.readAt(ctx.buffer, highBlock * Constants.keyBlockSize, Constants.maxKeySize + 2)
            return if (compareKeys(key, ctx.buffer) < 0) lowBlock else highBlock
        }

        val midBlock = (lowBlock + highBlock) / 2
        keyFile.readAt(ctx.buffer, midBlock * Constants.keyBlockSize, Constants.maxKeySize + 2)

        return if (compareKeys(key, ctx.buffer) < 0) {
            binarySearch0(lowBlock, midBlock - 1, key, ctx)
        } else {
            binarySearch0(midBlock, highBlock, key, ctx)
        }
    }

    fun scanBlock(block: Long, key: ByteArray, ctx: ScanContext): DiskSegment.OffsetLen? {
        ctx.reset()
        keyFile.readAt(ctx.buffer, block * Constants.keyBlockSize, Constants.keyBlockSize)

        while (true) {
            val keyLen = ctx.`is`.readShort().toInt() and 0xFFFF
            if (keyLen == Constants.endOfBlock) return null

            CompressedKey.decodeKey(ctx.key, keyLen, ctx.`is`)
            when (ctx.key.compare(key)) {
                0 -> return DiskSegment.OffsetLen(
                    ctx.`is`.readLong(),
                    ctx.`is`.readInt()
                )
                else -> ctx.`is`.skip(12)
            }
        }
    }

    /**
     * Returns the lower ID for the segment.
     *
     * @return The lower ID for the segment.
     */
    override fun lowerID(): Long {
        return lowerId
    }

    /**
     * Returns the upper ID for the segment.
     *
     * @return The upper ID for the segment.
     */
    override fun upperID(): Long {
        return upperId
    }

    class OffsetLen(val offset: Long, val len: Int)
    class LowHigh(val low: Long, val high: Long)

    class ScanContext {
        val key = KeyBuffer()
        val buffer = ByteArray(Constants.keyBlockSize)
        val `is` = LittleEndianDataInputStream(buffer)

        fun reset() {
            key.clear()
            `is`.reset()
        }
    }

    companion object {
        val bufferCache: ThreadLocal<ScanContext> = object : ThreadLocal<ScanContext>() {
            override fun initialValue() = ScanContext()
        }

        /**
         * Trims the suffix from a string if it exists.
         *
         * @param s The string to be modified.
         * @param suffix The suffix to remove.
         * @return The string with the suffix removed, if it exists.
         */
        fun trimSuffix(s: String, suffix: String): String =
            if (s.endsWith(suffix)) s.removeSuffix(suffix) else s

        /**
         * Trims the prefix from a string if it exists.
         *
         * @param s The string to be modified.
         * @param prefix The prefix to remove.
         * @return The string with the prefix removed, if it exists.
         */
        fun trimPrefix(s: String, prefix: String): String =
            if (s.startsWith(prefix)) s.removePrefix(prefix) else s

        /**
         * Removes a file if it exists, throwing an IOException if it cannot be deleted.
         *
         * @param path The directory path.
         * @param filename The name of the file within the directory.
         * @throws IOException if the file cannot be deleted.
         */
        @Throws(IOException::class)
        fun removeFileIfExists(path: Path, filename: String) {
            val file = path.resolve(filename)
            try {
                if (Files.exists(file) && !Files.deleteIfExists(file)) {
                    throw IOException("Unable to delete $file")
                }
            } catch (e: IOException) {
                throw IOException("Unable to delete $file", e)
            }
        }

        /**
         * Removes a file if it exists, throwing an IOException if it cannot be deleted.
         *
         * @param path The directory path.
         * @throws IOException if the file cannot be deleted.
         */
        @Throws(IOException::class)
        fun removeFileIfExists(file: Path) {
            try {
                if (Files.exists(file) && !Files.deleteIfExists(file)) {
                    throw IOException("Unable to delete $file")
                }
            } catch (e: IOException) {
                throw IOException("Unable to delete $file", e)
            }
        }

        /**
         * Loads the key index for a disk segment.
         *
         * @param keyFile The memory-mapped file containing the keys.
         * @param keyBlocks The number of key blocks.
         * @return A list of ByteArrays representing the key index.
         */
        fun loadKeyIndex(keyFile: MemoryMappedFile, keyBlocks: Long): List<ByteArray?> {
            if (keyFile.length() == 0L) return emptyList()

            val buffer = ByteArray(Constants.keyBlockSize)
            val keyIndex = mutableListOf<ByteArray?>()

            var block = 0L
            while (block < keyBlocks) {
                keyFile.readAt(buffer, block * Constants.keyBlockSize, Constants.maxKeySize + 2)
                val `is` = LittleEndianDataInputStream(buffer)
                val keyLen = `is`.readShort().toInt() and 0xFFFF
                if (keyLen == Constants.endOfBlock) break
                ByteArray(keyLen).also {
                    `is`.readFully(it, 0, keyLen)
                    keyIndex.add(it)
                }
                block += Constants.keyIndexInterval
            }

            return keyIndex
        }

        /**
         * Loads disk segments from the specified path.
         *
         * @param path The path to the directory containing the segments.
         * @param options The options to be used when loading the segments.
         * @return A list of loaded segments.
         */
        fun loadDiskSegments(path: Path, options: Options): List<Segment> {
            val segments = mutableListOf<Segment>()

            require(Files.isDirectory(path)) { "$path is not a directory" }

            // Clean up orphaned temp files
            Files.list(path).use { files ->
                files.forEach { file ->
                    val fileName = file.fileName.toString()
                    if (!fileName.endsWith(".tmp")) return@forEach

                    val base = DiskSegment.trimSuffix(fileName, ".tmp").let {
                        if (it.startsWith("keys.")) trimPrefix(it, "keys.")
                        else trimPrefix(it, "data.")
                    }

                    listOf("keys", "data").forEach { type ->
                        removeFileIfExists(path.resolve("$type.$base"))
                        removeFileIfExists(path.resolve("$type.$base.tmp"))
                    }
                }
            }

            Files.list(path).use { files ->
                files.forEach { file ->
                    val name = file.fileName.toString()
                    when {
                        name.startsWith("log.") -> segments.add(LogSegment(file.toString(), options))
                        name.startsWith("keys.") -> {
                            val segs = trimPrefix(name, "keys.")
                            segments.add(
                                DiskSegment(
                                    path.resolve("keys.$segs").toString(),
                                    path.resolve("data.$segs").toString(),
                                    null
                                )
                            )
                        }
                    }
                }
            }

            segments.sortWith(compareBy<Segment> { it.upperID() }.thenByDescending { it.lowerID() })

            // Prune older duplicate segments
            var i = 0
            while (i < segments.size) {
                val seg = segments[i]
                val overlapping = segments.subList(i + 1, segments.size).firstOrNull {
                    seg.lowerID() >= it.lowerID() && seg.upperID() <= it.upperID()
                }
                if (overlapping != null) {
                    segments.removeAt(i)
                    seg.removeSegment()
                } else {
                    i++
                }
            }

            return segments
        }


        /**
         * Creates a Removable for the DiskSegment, allowing it to be cleaned up later.
         *
         * @param ds The DiskSegment to be removed.
         * @return A Removable instance for the segment.
         */
        private fun createRemovable(ds: DiskSegment): Removable = object :
            Removable {
            override fun remove() {
                ds.keyFile.close()
                ds.dataFile.close()
                Files.deleteIfExists(Path.of(ds.keyFilename))
                Files.deleteIfExists(Path.of(ds.dataFilename))
            }

            override fun toString(): String = "DiskSegment:${ds.keyFilename},${ds.dataFilename}"
        }

        /**
         * Compares two keys.
         *
         * @param b The first key.
         * @param buffer The second key.
         * @return A comparison result as an integer: negative if b is less than buffer, zero if equal, positive if greater.
         */
        private fun compareKeys(b: ByteArray, buffer: ByteArray): Int {
            val len = (buffer[0].toInt() shl (buffer[1].toInt() shl 8)).toShort().toInt()
            for (i in 0 until len) {
                if (i == b.size) return -1
                val result = b[i].compareTo(buffer[i + 2])
                if (result != 0) return result
            }
            return when {
                len < b.size -> 1
                else -> 0
            }
        }
    }
}
