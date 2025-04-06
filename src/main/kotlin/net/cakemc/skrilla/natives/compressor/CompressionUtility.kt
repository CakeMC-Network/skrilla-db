package net.cakemc.skrilla.natives.compressor

import com.sun.jna.Native

object CompressionUtility {
    private val library: CompressionLibrary = Native.loadLibrary(
        if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            "compression"
        } else {
            "lib_compression"
        },
        CompressionLibrary::class.java
    )

    /**
     * Compresses the input data using the native compression function.
     *
     * @param input The data to compress.
     * @return The compressed data as a byte array.
     */
    fun compress(input: String): ByteArray {
        val inputLen = input.length
        val output = ByteArray(1024)
        val outputLen = IntArray(1)

        val result = library.compress_data(input, inputLen, output, outputLen)
        if (result != 0) {
            throw IllegalStateException("Compression failed with error code: $result")
        }

        return output.copyOf(outputLen[0]) // Return the compressed data
    }

    /**
     * Decompresses the input data using the native decompression function.
     *
     * @param input The compressed data to decompress.
     * @return The decompressed data as a string.
     */
    fun decompress(input: String): String {
        val inputLen = input.length
        val output = ByteArray(1024)
        val outputLen = IntArray(1)

        val result = library.decompress_data(input, inputLen, output, outputLen)
        if (result != 0) {
            throw IllegalStateException("Decompression failed with error code: $result")
        }

        return String(output, 0, outputLen[0])
    }
}