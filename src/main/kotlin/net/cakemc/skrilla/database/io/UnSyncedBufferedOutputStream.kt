package net.cakemc.skrilla.database.io

import java.io.*

/**
 * The class implements a buffered output stream without any synchronization.
 * This is a custom implementation of a buffered output stream that allows for more efficient writes
 * to the underlying output stream by buffering the data in an internal buffer.
 */
class UnSyncedBufferedOutputStream @JvmOverloads constructor(
    out: OutputStream?,
    size: Int = DEFAULT_MAX_BUFFER_SIZE
) : FilterOutputStream(out) {

    /**
     * The internal buffer where data is stored.
     * The buffer temporarily holds data before it is written to the underlying output stream.
     */
    protected var buf: ByteArray = ByteArray(size).also {
        require(size > 0) { "Buffer size <= 0" }
    }

    /**
     * The number of valid bytes in the buffer.
     * Elements buf[0] through buf[count - 1] contain valid byte data.
     */
    protected var count: Int = 0

    /**
     * Flushes the internal buffer to the underlying output stream.
     *
     * This method writes the buffered data to the output stream and resets the count to zero.
     */
    @Throws(IOException::class)
    private fun flushBuffer() {
        if (count > 0) {
            out.write(buf, 0, count)
            count = 0
        }
    }

    /**
     * Writes a single byte to this buffered output stream.
     * The byte is stored in the internal buffer. If the buffer is full, it is flushed to the output stream.
     *
     * @param b The byte to be written.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun write(b: Int) {
        implWrite(b)
    }

    @Throws(IOException::class)
    private fun implWrite(b: Int) {
        if (count >= buf.size) flushBuffer()
        buf[count++] = b.toByte()
    }

    /**
     * Writes `len` bytes from the specified byte array starting at offset `off`.
     * The data is buffered in the internal buffer. If the buffer is full, it is flushed to the output stream.
     *
     * @param b The byte array to write from.
     * @param off The offset to start reading from the array.
     * @param len The number of bytes to write.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        implWrite(b, off, len)
    }

    @Throws(IOException::class)
    private fun implWrite(b: ByteArray, off: Int, len: Int) {
        require(off >= 0 && len >= 0 && off + len <= b.size) { "Index out of bounds" }

        if (len >= buf.size) {
            flushBuffer()
            out.write(b, off, len)
        } else {
            if (len > buf.size - count) flushBuffer()
            b.copyInto(buf, destinationOffset = count, startIndex = off, endIndex = off + len)
            count += len
        }
    }

    /**
     * Flushes the stream, writing any buffered data to the underlying output stream.
     * This ensures that all data in the internal buffer is written to the output stream.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun flush() {
        implFlush()
    }

    @Throws(IOException::class)
    private fun implFlush() {
        flushBuffer()
        out.flush()
    }

    companion object {
        private const val DEFAULT_MAX_BUFFER_SIZE = 8192
    }
}
