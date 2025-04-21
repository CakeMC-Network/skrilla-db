package net.cakemc.skrilla.database.imdb

import java.io.*

/**
 * Provides functionality to decode compressed keys.
 * This object is used to handle the decoding of keys from an input stream, including the handling of compressed keys.
 */
internal object CompressedKey {

    /**
     * Decodes a compressed key from the input stream and stores it in the provided KeyBuffer.
     * If the key is compressed, it extracts the prefix and compressed data.
     * If the key is not compressed, it reads the key directly into the buffer.
     *
     * @param dst The `KeyBuffer` that holds the previous key and receives the new key.
     * @param keyLength The length of the encoded key (may include compression flags).
     * @param `is` The input stream to read the key from.
     * @throws IOException If an error occurs while reading from the input stream.
     */
    @Throws(IOException::class)
    fun decodeKey(dst: KeyBuffer, keyLength: Int, `is`: InputStream) {
        // Determine if the key is compressed based on the compression bit
        val isCompressed = (keyLength and Constants.compressedBit) != 0

        if (isCompressed) {
            // Extract prefix length and compressed data length
            val prefixLen = (keyLength shr 8) and 0xFFFF and Constants.maxPrefixLen
            val compressedLen = keyLength and Constants.maxCompressedLen
            // Read the compressed data into the buffer
            dst.from(`is`, compressedLen, prefixLen)
        } else {
            // Read the uncompressed key directly into the buffer
            dst.from(`is`, keyLength, 0)
        }
    }
}
