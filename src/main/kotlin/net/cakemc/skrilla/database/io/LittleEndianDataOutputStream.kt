package net.cakemc.skrilla.database.io

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * A custom output stream that writes data in Little Endian byte order.
 * This class behaves similarly to the `DataOutputStream` but ensures that data is written
 * using Little Endian byte order for primitive types such as `short`, `int`, and `long`.
 *
 * @param out the underlying output stream to write data to.
 */
class LittleEndianDataOutputStream(out: OutputStream?) : FilterOutputStream(out) {
    private val writeBuffer = ByteArray(8)

    /**
     * Writes a `short` (16-bit integer) in Little Endian byte order.
     *
     * @param v the `short` value to write.
     * @throws IOException if an I/O error occurs during writing.
     */
    @Throws(IOException::class)
    fun writeShort(v: Int) {
        writeBuffer[0] = (v ushr 0).toByte()
        writeBuffer[1] = (v ushr 8).toByte()
        out.write(writeBuffer, 0, 2)
    }

    /**
     * Writes an `int` (32-bit integer) in Little Endian byte order.
     *
     * @param v the `int` value to write.
     * @throws IOException if an I/O error occurs during writing.
     */
    @Throws(IOException::class)
    fun writeInt(v: Int) {
        writeBuffer[0] = (v ushr 0).toByte()
        writeBuffer[1] = (v ushr 8).toByte()
        writeBuffer[2] = (v ushr 16).toByte()
        writeBuffer[3] = (v ushr 24).toByte()
        out.write(writeBuffer, 0, 4)
    }

    /**
     * Writes a `long` (64-bit integer) in Little Endian byte order.
     *
     * @param v the `long` value to write.
     * @throws IOException if an I/O error occurs during writing.
     */
    @Throws(IOException::class)
    fun writeLong(v: Long) {
        writeBuffer[0] = (v ushr 0).toByte()
        writeBuffer[1] = (v ushr 8).toByte()
        writeBuffer[2] = (v ushr 16).toByte()
        writeBuffer[3] = (v ushr 24).toByte()
        writeBuffer[4] = (v ushr 32).toByte()
        writeBuffer[5] = (v ushr 40).toByte()
        writeBuffer[6] = (v ushr 48).toByte()
        writeBuffer[7] = (v ushr 56).toByte()
        out.write(writeBuffer, 0, 8)
    }
}
