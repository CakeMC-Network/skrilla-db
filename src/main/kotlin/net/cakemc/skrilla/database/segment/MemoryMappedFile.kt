package net.cakemc.skrilla.database.segment

import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.min

/**
 * MemoryMappedFile enables fast, read-only access to large files by
 * memory-mapping them into 1GB segments.
 *
 * Represents a memory-mapped file that allows efficient, read-only access
 * to large files by mapping them into memory in segments.
 *
 * The file is divided into 1GB chunks, each backed by a ByteBuffer in LITTLE_ENDIAN order.
 * This class provides methods to read specific portions of the file into byte arrays.
 *
 * @param file the file to memory map
 */
class MemoryMappedFile(file: RandomAccessFile) {

    /** Maximum size of each mapped memory segment (1GB) */
    private val MAX_MAP_SIZE = 1024 * 1024 * 1024L

    /** Total length of the mapped file */
    private val fileLength: Long = file.length()

    /** Array of mapped byte buffers representing segments of the file */
    private val mappedBuffers: Array<ByteBuffer?>

    /** File channel used to map the file into memory */
    private val fileChannel: FileChannel = file.channel

    /** Indicates whether the file has been closed */
    @Volatile
    private var isClosed = false

    init {
        val segmentCount = ((fileLength + MAX_MAP_SIZE - 1) / MAX_MAP_SIZE).toInt()
        mappedBuffers = arrayOfNulls(segmentCount)
        var remainingBytes = fileLength

        for (index in 0 until segmentCount) {
            val mapSize = min(remainingBytes, MAX_MAP_SIZE)
            mappedBuffers[index] = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                index * MAX_MAP_SIZE,
                mapSize
            ).order(ByteOrder.LITTLE_ENDIAN)
            remainingBytes -= mapSize
        }
    }

    /**
     * Returns the total length of the mapped file.
     *
     * @return file length in bytes
     */
    fun length(): Long = fileLength

    /**
     * Reads the entire [dst] buffer from the specified file [position].
     *
     * @param dst destination byte array
     * @throws IOException if the file is closed or an error occurs during read
     */
    @Throws(IOException::class)
    fun readAt(dst: ByteArray, position: Long) {
        readAt(dst, position, dst.size)
    }

    /**
     * Reads [n] bytes from the specified [position] into [dst].
     *
     * @param dst the destination byte array
     * @param position the position in the file to start reading from
     * @param n the number of bytes to read
     * @return the number of bytes actually read
     * @throws IOException if the file is closed or an error occurs during read
     */
    @Throws(IOException::class)
    fun readAt(dst: ByteArray?, position: Long, n: Int): Int {
        requireNotNull(dst) { "Destination buffer cannot be null" }
        if (isClosed) throw IOException("Memory mapped file is closed")

        var readPosition = position
        var remaining = n
        var totalRead = 0

        while (remaining > 0) {
            val segmentIndex = (readPosition / MAX_MAP_SIZE).toInt()
            val segmentOffset = (readPosition % MAX_MAP_SIZE).toInt()
            val buffer = mappedBuffers[segmentIndex] ?: break

            val bytesToRead = min(remaining, buffer.capacity() - segmentOffset)
            buffer.position(segmentOffset)
            buffer.get(dst, totalRead, bytesToRead)

            readPosition += bytesToRead
            totalRead += bytesToRead
            remaining -= bytesToRead
        }

        return totalRead
    }

    /**
     * Closes the memory-mapped file and releases associated resources.
     *
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun close() {
        if (isClosed) return
        isClosed = true

        mappedBuffers.forEach { buffer ->
            buffer?.clear()
        }

        fileChannel.close()
    }
}
