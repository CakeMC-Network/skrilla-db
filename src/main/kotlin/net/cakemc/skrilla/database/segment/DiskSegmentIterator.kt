package net.cakemc.skrilla.database.segment

import net.cakemc.skrilla.database.imdb.CompressedKey
import net.cakemc.skrilla.database.imdb.Constants
import net.cakemc.skrilla.database.imdb.KeyBuffer
import net.cakemc.skrilla.database.imdb.KeyValue
import net.cakemc.skrilla.database.io.LittleEndianDataInputStream
import net.cakemc.skrilla.database.lookup.LookupIterator
import java.io.IOException

/**
 * DiskSegmentIterator is an implementation of [LookupIterator] that traverses key-value entries
 * stored in a [DiskSegment].
 *
 * @property segment the DiskSegment this iterator reads from
 * @property lower the optional lower bound for keys
 * @property upper the optional upper bound for keys
 * @property buffer reusable buffer for block data
 * @property block the current block index
 */
internal class DiskSegmentIterator(
    val segment: DiskSegment,
    val lower: ByteArray?,
    val upper: ByteArray?,
    val buffer: ByteArray,
    var block: Long
) : LookupIterator {

    /** Temporary key buffer used during decoding */
    private val keyBuffer: KeyBuffer = KeyBuffer()

    /** Currently loaded key */
    private var key: ByteArray? = null

    /** Currently loaded value */
    private var data: ByteArray? = null

    /** Input stream wrapping the buffer */
    private var inputStream: LittleEndianDataInputStream = LittleEndianDataInputStream(buffer)

    /** Indicates if the current key-value pair is valid */
    private var isValid = false

    /** Indicates if the iterator has finished reading all entries */
    private var finished = false

    /**
     * Peeks at the next key without advancing the iterator.
     *
     * @return the next key as a byte array, or null if none available
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun peekKey(): ByteArray? {
        return if (isValid) {
            key
        } else {
            nextKeyValue()
            key
        }
    }

    /**
     * Advances the iterator and returns the next key-value pair.
     *
     * @return the next [KeyValue], or null if there are no more entries
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun next(): KeyValue? {
        if (finished) return null

        return if (isValid) {
            isValid = false
            KeyValue(key!!, data!!)
        } else {
            try {
                if (nextKeyValue()) null
                else KeyValue(key!!, data!!)
            } finally {
                isValid = false
            }
        }
    }

    /**
     * Attempts to load the next key-value pair from the buffer.
     *
     * @return true if no more data is available, false otherwise
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    private fun nextKeyValue(): Boolean {
        if (finished) return true

        while (true) {
            val keyLength = inputStream.readShort().toInt() and 0xFFFF
            if (keyLength == Constants.endOfBlock) {
                return handleEndOfBlock()
            }

            CompressedKey.decodeKey(keyBuffer, keyLength, inputStream)

            val dataOffset = inputStream.readLong()
            val dataLength = inputStream.readInt()

            if (shouldSkipKey(keyBuffer)) continue

            key = keyBuffer.toBytes()
            data = ByteArray(dataLength)

            if (segment.dataFile.readAt(data, dataOffset, dataLength) != dataLength) {
                throw IOException("Unable to read data file, expecting $dataLength, but read ${data!!.size}")
            }

            isValid = true
            return false
        }
    }

    /**
     * Determines whether the current key should be skipped based on range bounds.
     *
     * @param keyBuffer the current decoded key
     * @return true if the key should be skipped
     */
    private fun shouldSkipKey(keyBuffer: KeyBuffer): Boolean {
        return (lower != null && keyBuffer.compare(lower) < 0) ||
                (upper != null && keyBuffer.compare(upper) > 0).also {
                    if (it) {
                        finished = true
                        isValid = true
                        key = null
                        data = null
                    }
                }
    }

    /**
     * Handles transitioning to the next block when the end-of-block marker is reached.
     *
     * @return true if all blocks are exhausted, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private fun handleEndOfBlock(): Boolean {
        block++
        if (block == segment.keyBlocks) {
            finished = true
            key = null
            data = null
            isValid = true
            return true
        }

        val length = segment.keyFile.readAt(buffer, block * Constants.keyBlockSize, Constants.keyBlockSize)
        inputStream = LittleEndianDataInputStream(buffer, 0, length)
        if (inputStream.available() != Constants.keyBlockSize) throw IOException("Unable to read key file")
        return false
    }
}
