package net.cakemc.skrilla.database.compression

import com.github.luben.zstd.Zstd
import net.cakemc.database.compression.FileCompression

/**
 * The ZStdCompression class is an implementation of [FileCompression] that uses the
 * Zstandard (ZStd) compression algorithm to compress and decompress byte arrays.
 *
 * Zstandard is a fast and efficient compression algorithm that provides a good balance
 * between compression ratio and speed. It is especially effective for large datasets.
 *
 * @see net.cakemc.database.compression.FileCompression
 * @see com.github.luben.zstd.Zstd
 */
class ZStdCompression : FileCompression() {

    /**
     * Compresses the given byte array using the Zstandard (ZStd) algorithm.
     *
     * @param source The byte array to be compressed.
     * @return A byte array containing the compressed data.
     */
    override fun compress(source: ByteArray): ByteArray {
        return Zstd.compress(source)
    }

    /**
     * Decompresses the given byte array using the Zstandard (ZStd) algorithm.
     *
     * @param source The byte array to be decompressed.
     * @return A byte array containing the decompressed data.
     */
    override fun decompress(source: ByteArray): ByteArray {
        return Zstd.decompress(source, source.size * 10)
    }
}
