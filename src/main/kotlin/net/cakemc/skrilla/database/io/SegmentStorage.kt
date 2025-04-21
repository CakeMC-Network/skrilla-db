package net.cakemc.skrilla.database.io

import net.cakemc.skrilla.database.imdb.Constants
import net.cakemc.skrilla.database.segment.DiskSegment
import net.cakemc.skrilla.database.lookup.LookupIterator
import net.cakemc.skrilla.database.segment.MemorySegment
import java.io.*

/**
 * Handles operations related to storing and managing segments on disk.
 */
internal object SegmentStorage {

    /**
     * Writes a segment to disk and returns a corresponding [DiskSegment].
     *
     * @param dbPath the path of the database where the segment should be stored.
     * @param segment the [MemorySegment] to be written to disk.
     * @return a [DiskSegment] if the segment was successfully written, or `null` if the segment is empty.
     * @throws IOException if an I/O error occurs during the writing process.
     */
    @Throws(IOException::class)
    fun writeSegmentToDisk(dbPath: String?, segment: MemorySegment): net.cakemc.skrilla.database.segment.DiskSegment? {
        val iterator = segment.lookup(null, null)

        if (iterator.peekKey() == null) {
            segment.removeSegment()
            return null
        }

        val lowerId = segment.lowerID()
        val upperId = segment.upperID()

        val keyFilename = "$dbPath/keys.$lowerId.$upperId"
        val dataFilename = "$dbPath/data.$lowerId.$upperId"

        val diskSegment = writeAndLoadSegment(keyFilename, dataFilename, iterator, false)
        segment.removeSegment()
        return diskSegment
    }

    /**
     * Writes segment files to disk and loads the [DiskSegment].
     *
     * @param keyFilename the filename for the keys.
     * @param dataFilename the filename for the data.
     * @param iterator the iterator used to traverse the segment.
     * @param removeDeleted indicates whether deleted entries should be removed.
     * @return the corresponding [DiskSegment] created from the segment files.
     * @throws IOException if an I/O error occurs during the process.
     */
    @Throws(IOException::class)
    fun writeAndLoadSegment(
        keyFilename: String,
        dataFilename: String,
        iterator: LookupIterator,
        removeDeleted: Boolean
    ): net.cakemc.skrilla.database.segment.DiskSegment {
        val keyFileTmp = File("$keyFilename.tmp")
        val dataFileTmp = File("$dataFilename.tmp")

        val keyIndex: List<ByteArray?> = try {
            writeSegmentFiles(keyFileTmp, dataFileTmp, iterator, removeDeleted)
        } catch (e: IOException) {
            keyFileTmp.delete()
            dataFileTmp.delete()
            throw e
        }

        keyFileTmp.renameTo(File(keyFilename))
        dataFileTmp.renameTo(File(dataFilename))

        return net.cakemc.skrilla.database.segment.DiskSegment(keyFilename, dataFilename, keyIndex)
    }

    /**
     * Writes the segment data to temporary files and returns a list of key indices.
     *
     * @param keyFile the temporary file for storing keys.
     * @param dataFile the temporary file for storing data.
     * @param iterator the iterator used to traverse the segment.
     * @param removeDeleted indicates whether deleted entries should be removed.
     * @return a list of byte arrays representing the key indices.
     * @throws IOException if an I/O error occurs during the writing process.
     */
    @Throws(IOException::class)
    fun writeSegmentFiles(
        keyFile: File,
        dataFile: File,
        iterator: LookupIterator,
        removeDeleted: Boolean
    ): List<ByteArray?> {
        val keyWriter = LittleEndianDataOutputStream(UnSyncedBufferedOutputStream(FileOutputStream(keyFile)))
        val dataWriter = LittleEndianDataOutputStream(UnSyncedBufferedOutputStream(FileOutputStream(dataFile)))

        var dataOffset: Long = 0
        var keyBlockLength = 0
        var keyCount = 0
        var block = 0
        val zeros = ByteArray(Constants.keyBlockSize)
        var previousKey: ByteArray? = null

        val keyIndex = mutableListOf<ByteArray?>()

        while (true) {
            val keyValue = iterator.next() ?: break
            val value = keyValue.value ?: continue
            val key = keyValue.key ?: continue

            if (removeDeleted && value.isEmpty()) continue

            keyCount++
            dataWriter.write(value)

            // Check if key fits in current block
            if (keyBlockLength + 2 + key.size + 8 + 4 >= Constants.keyBlockSize - 2) {
                keyWriter.writeShort(Constants.endOfBlock)
                keyBlockLength += 2
                keyWriter.write(zeros, 0, Constants.keyBlockSize - keyBlockLength)
                keyBlockLength = 0
                previousKey = null
            }

            // Add key to index if needed
            if (keyBlockLength == 0 && block % Constants.keyIndexInterval == 0) {
                keyIndex.add(key.clone())
            }

            block++

            val diskKey = encodeKey(key, previousKey)
            previousKey = key.clone()

            keyWriter.writeShort(diskKey.keylen)
            keyWriter.write(diskKey.compressedKey)
            keyWriter.writeLong(dataOffset)
            keyWriter.writeInt(value.size)

            keyBlockLength += 2 + diskKey.compressedKey.size + 8 + 4
            dataOffset += value.size
        }

        // Pad final key block
        if (keyBlockLength in 1 until Constants.keyBlockSize) {
            keyWriter.writeShort(Constants.endOfBlock)
            keyBlockLength += 2
            keyWriter.write(zeros, 0, Constants.keyBlockSize - keyBlockLength)
        }

        keyWriter.flush()
        dataWriter.flush()

        return keyIndex
    }

    /**
     * Encodes a key with respect to the previous key in the sequence.
     *
     * @param key the current key to be encoded.
     * @param previousKey the previous key in the sequence.
     * @return a [DiskKey] containing the encoded key data.
     */
    private fun encodeKey(key: ByteArray, previousKey: ByteArray?): DiskKey {
        val prefixLength = calculatePrefixLength(previousKey, key)

        return if (prefixLength > 0) {
            val compressedKey = key.copyOfRange(prefixLength, key.size)
            DiskKey(Constants.compressedBit or (prefixLength shl 8) or compressedKey.size, compressedKey)
        } else {
            DiskKey(key.size, key)
        }
    }

    /**
     * Calculates the prefix length for key compression by comparing the current key with the previous key.
     *
     * @param previousKey the previous key.
     * @param key the current key.
     * @return the length of the common prefix between the two keys.
     */
    private fun calculatePrefixLength(previousKey: ByteArray?, key: ByteArray): Int {
        if (previousKey == null) return 0

        var length = 0
        while (length < previousKey.size && length < key.size && previousKey[length] == key[length]) {
            length++
        }

        return if (length > Constants.maxPrefixLen || key.size - length > Constants.maxCompressedLen) {
            0
        } else {
            length
        }
    }

    /**
     * Represents a compressed disk key with its length and the actual key data.
     *
     * @param keylen the length of the key.
     * @param compressedKey the compressed key data.
     */
    private class DiskKey(val keylen: Int, val compressedKey: ByteArray)
}
