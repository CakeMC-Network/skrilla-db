package net.cakemc.skrilla.database.imdb

import java.io.*
import java.util.*

/**
 * Efficiently handles prefix key compression.
 * This class provides functionality to read data into a buffer, compare the buffer with other byte arrays,
 * and return a copy of the buffer's content.
 */
internal class KeyBuffer {

    /**
     * The internal buffer to store bytes.
     */
    private val buffer: ByteArray = ByteArray(1024)

    /**
     * The length of the valid data in the buffer.
     */
    var len: Int = 0
        private set

    /**
     * Compares the current buffer's content with the provided byte array.
     *
     * @param bytes The byte array to compare with the buffer's content.
     * @return A negative integer, zero, or a positive integer as the current buffer is less than, equal to,
     *         or greater than the provided byte array.
     */
    fun compare(bytes: ByteArray): Int {
        return Arrays.compare(buffer, 0, len, bytes, 0, bytes.size)
    }

    /**
     * Reads bytes from the provided input stream into the buffer starting at the specified offset.
     *
     * @param src The input stream from which to read bytes.
     * @param len The number of bytes to read.
     * @param offset The offset at which to start writing in the buffer.
     * @throws IOException If an error occurs while reading from the input stream.
     */
    @Throws(IOException::class)
    fun from(src: InputStream, len: Int, offset: Int) {
        if (offset + len > buffer.size) throw IOException("Attempting to read beyond buffer limit.")
        src.read(buffer, offset, len)
        this.len = offset + len
    }

    /**
     * Clears the buffer, resetting its length to zero.
     */
    fun clear() {
        len = 0
    }

    /**
     * Returns a copy of the buffer's content as a byte array.
     *
     * @return A byte array containing the data currently in the buffer.
     */
    fun toBytes(): ByteArray {
        return buffer.copyOf(len)
    }

}
