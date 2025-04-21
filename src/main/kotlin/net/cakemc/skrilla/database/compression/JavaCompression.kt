package net.cakemc.database.compression

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * JavaCompression is an implementation of [FileCompression] that uses the Java standard
 * compression and decompression libraries, specifically [Deflater] and [Inflater],
 * to perform data compression and decompression.
 *
 * This class provides two main functionalities:
 * - **Compression**: The `compress()` method compresses the given byte array using the
 *   `Deflater` class to reduce its size.
 * - **Decompression**: The `decompress()` method restores the original byte array
 *   from the compressed byte array using the `Inflater` class.
 *
 * Unlike GZIP compression, this class works directly with the `Deflater` and `Inflater`
 * classes and doesn't wrap the data in a GZIP-specific header or trailer. It is suitable
 * for use cases where GZIP headers are not required.
 *
 * @see java.util.zip.Deflater
 * @see java.util.zip.Inflater
 */
class JavaCompression : FileCompression() {
    private val deflater = Deflater(Deflater.DEFAULT_COMPRESSION)
    private val inflater = Inflater()

    /**
     * Compresses the given byte array using the [Deflater] class.
     *
     * This method compresses the input data by feeding it into the `Deflater` class,
     * which reduces the size of the data to be returned as a byte array.
     *
     * @param data The byte array to compress.
     * @return A byte array containing the compressed data.
     * @throws RuntimeException If an error occurs during the compression process.
     */
    override fun compress(data: ByteArray): ByteArray {
        deflater.reset()

        deflater.setInput(data)
        deflater.finish()

        val outputStream = ByteArrayOutputStream(data.size)

        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }

        try {
            outputStream.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return outputStream.toByteArray()
    }

    /**
     * Decompresses the given compressed byte array using the [Inflater] class.
     *
     * This method restores the original data by inflating the compressed byte array
     * using the `Inflater` class, returning the decompressed byte array.
     *
     * @param compressedData The byte array containing compressed data to decompress.
     * @return A byte array containing the decompressed data.
     * @throws RuntimeException If an error occurs during the decompression process.
     */
    override fun decompress(compressedData: ByteArray): ByteArray {
        inflater.reset()

        inflater.setInput(compressedData)

        val outputStream = ByteArrayOutputStream(compressedData.size)

        try {
            val buffer = ByteArray(1024)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }

            outputStream.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: DataFormatException) {
            throw RuntimeException(e)
        }

        return outputStream.toByteArray()
    }
}
