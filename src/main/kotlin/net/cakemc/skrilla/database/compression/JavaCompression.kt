package net.cakemc.database.compression

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * The type Gzip compression.
 */
class JavaCompression : FileCompression() {
    private val deflater = Deflater(Deflater.DEFAULT_COMPRESSION)
    private val inflater = Inflater()

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
