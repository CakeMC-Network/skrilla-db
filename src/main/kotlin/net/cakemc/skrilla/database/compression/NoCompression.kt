package net.cakemc.skrilla.database.compression

import net.cakemc.database.compression.FileCompression

/**
 * The NoCompression class is an implementation of [FileCompression] that performs no
 * compression or decompression. It simply returns the input byte array as the result.
 *
 * This class is useful when compression is not required or when you want to bypass
 * compression in certain scenarios, but still need to provide a consistent interface
 * for compression and decompression.
 *
 * @see net.cakemc.database.compression.FileCompression
 */
class NoCompression : FileCompression() {

    /**
     * Returns the input byte array without any compression.
     *
     * @param source The byte array to be "compressed" (in reality, no compression is done).
     * @return The same byte array that was passed as input.
     */
    override fun compress(source: ByteArray): ByteArray {
        return source
    }

    /**
     * Returns the input byte array without any decompression.
     *
     * @param source The byte array to be "decompressed" (in reality, no decompression is done).
     * @return The same byte array that was passed as input.
     */
    override fun decompress(source: ByteArray): ByteArray {
        return source
    }
}
