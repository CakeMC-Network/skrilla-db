package net.cakemc.database.compression

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * GzipCompression is an implementation of [FileCompression] that uses the GZIP format
 * for compressing and decompressing byte arrays.
 *
 * The class provides two main functionalities:
 * - **Compression**: The `compress()` method compresses the given byte array into a smaller GZIP-encoded byte array.
 * - **Decompression**: The `decompress()` method restores the original data from a GZIP-compressed byte array.
 *
 * GZIP is a widely used compression format that is effective for reducing the size of data,
 * commonly used for text-based data, logs, and files where compression efficiency is important.
 *
 * @see java.util.zip.GZIPOutputStream
 * @see java.util.zip.GZIPInputStream
 */
class GzipCompression : FileCompression() {

    /**
     * Compresses the given byte array into a GZIP-compressed byte array.
     *
     * This method uses [GZIPOutputStream] to write the input data into a GZIP format.
     * The resulting compressed data can be stored or transferred more efficiently.
     *
     * @param data The byte array to compress.
     * @return A new byte array containing the compressed data in GZIP format.
     * @throws IOException If an error occurs during the compression process.
     */
    override fun compress(data: ByteArray): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val gzipOutputStream = GZIPOutputStream(byteArrayOutputStream)

        try {
            gzipOutputStream.write(data)
        } catch (e: IOException) {
            throw IOException("Error during compression", e)
        } finally {
            gzipOutputStream.close()
        }

        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Decompresses the given GZIP-compressed byte array back to its original form.
     *
     * This method uses [GZIPInputStream] to read and decompress the input data,
     * returning the original byte array before it was compressed.
     *
     * @param compressedData The GZIP-compressed byte array to decompress.
     * @return A new byte array containing the decompressed data.
     * @throws IOException If an error occurs during the decompression process.
     */
    override fun decompress(compressedData: ByteArray): ByteArray {
        val byteArrayInputStream = compressedData.inputStream()
        val gzipInputStream = GZIPInputStream(byteArrayInputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()

        try {
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (gzipInputStream.read(buffer).also { bytesRead = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
            }
        } catch (e: IOException) {
            throw IOException("Error during decompression", e)
        } finally {
            gzipInputStream.close()
        }

        return byteArrayOutputStream.toByteArray()
    }
}
