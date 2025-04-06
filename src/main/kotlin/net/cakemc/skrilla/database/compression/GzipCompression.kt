package net.cakemc.database.compression

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * The type Gzip compression.
 */
class GzipCompression : FileCompression() {

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
