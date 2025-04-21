package net.cakemc.skrilla.database.io

import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.IOException

/**
 * A custom `DataInputStream` that reads data in Little Endian byte order.
 * This class is not safe for concurrent use.
 *
 * It supports reading `short`, `int`, and `long` types in Little Endian format,
 * as well as general byte array reading.
 *
 * @param b the byte array to read from.
 * @param offset the offset from where to start reading the byte array (optional).
 * @param len the length of the byte array to read (optional).
 */
class LittleEndianDataInputStream : ByteArrayInputStream {
    private val readBuffer = ByteArray(8)

    constructor(b: ByteArray) : super(b)

    constructor(b: ByteArray, offset: Int, len: Int) : super(b, offset, len)

    /**
     * Reads data into the provided byte array until the specified length is reached.
     * This method throws an EOFException if the end of the stream is encountered before
     * the requested number of bytes is read.
     *
     * @param b the byte array to store the data.
     * @param off the starting offset in the byte array.
     * @param len the number of bytes to read.
     * @throws IOException if an I/O error occurs or the end of stream is reached unexpectedly.
     */
    @Throws(IOException::class)
    fun readFully(b: ByteArray, off: Int, len: Int) {
        if (len < 0) throw IndexOutOfBoundsException("Length must be non-negative")
        var n = 0
        while (n < len) {
            val count = read(b, off + n, len - n)
            if (count < 0) throw EOFException("Unexpected end of stream")
            n += count
        }
    }

    /**
     * Reads a `short` (16-bit signed integer) from the stream in Little Endian byte order.
     *
     * @return the `short` value read from the stream.
     * @throws IOException if an I/O error occurs or the end of stream is reached unexpectedly.
     */
    @Throws(IOException::class)
    fun readShort(): Short {
        ensureAvailable(2)
        val ch2 = buf[pos++].toInt() and 0xFF
        val ch1 = buf[pos++].toInt() and 0xFF
        return ((ch1 shl 8) or ch2).toShort()
    }

    /**
     * Reads an `int` (32-bit signed integer) from the stream in Little Endian byte order.
     *
     * @return the `int` value read from the stream.
     * @throws IOException if an I/O error occurs or the end of stream is reached unexpectedly.
     */
    @Throws(IOException::class)
    fun readInt(): Int {
        ensureAvailable(4)
        val ch4 = buf[pos++].toInt() and 0xFF
        val ch3 = buf[pos++].toInt() and 0xFF
        val ch2 = buf[pos++].toInt() and 0xFF
        val ch1 = buf[pos++].toInt() and 0xFF
        return (ch1 shl 24) or (ch2 shl 16) or (ch3 shl 8) or ch4
    }

    /**
     * Reads a `long` (64-bit signed integer) from the stream in Little Endian byte order.
     *
     * @return the `long` value read from the stream.
     * @throws IOException if an I/O error occurs or the end of stream is reached unexpectedly.
     */
    @Throws(IOException::class)
    fun readLong(): Long {
        readFully(readBuffer, 0, 8)
        return (readBuffer[7].toLong() shl 56) or
                ((readBuffer[6].toInt() and 0xFF).toLong() shl 48) or
                ((readBuffer[5].toInt() and 0xFF).toLong() shl 40) or
                ((readBuffer[4].toInt() and 0xFF).toLong() shl 32) or
                ((readBuffer[3].toInt() and 0xFF).toLong() shl 24) or
                ((readBuffer[2].toInt() and 0xFF).toLong() shl 16) or
                ((readBuffer[1].toInt() and 0xFF).toLong() shl 8) or
                ((readBuffer[0].toInt() and 0xFF).toLong())
    }

    /**
     * Skips over the specified number of bytes in the stream.
     *
     * @param n the number of bytes to skip.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O error occurs.
     */
    override fun skip(n: Long): Long {
        val skipAmount = n.coerceAtMost((count - pos).toLong()).toInt()
        pos += skipAmount
        return skipAmount.toLong()
    }

    /**
     * Reads a specified number of bytes into a byte array.
     *
     * @param b the byte array to store the read bytes.
     * @param off the starting offset in the byte array.
     * @param len the number of bytes to read.
     * @return the number of bytes actually read.
     * @throws IOException if an I/O error occurs or the end of stream is reached unexpectedly.
     */
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val available = count - pos
        if (len > available) throw EOFException("Attempt to read beyond available buffer")
        System.arraycopy(buf, pos, b, off, len)
        pos += len
        return len
    }

    /**
     * Ensures that there are enough bytes available to read.
     *
     * @param bytesNeeded the number of bytes that are required.
     * @throws EOFException if there are not enough bytes available.
     */
    private fun ensureAvailable(bytesNeeded: Int) {
        if (pos + bytesNeeded > count) {
            throw EOFException("Not enough bytes to read: needed $bytesNeeded, available ${count - pos}")
        }
    }
}
