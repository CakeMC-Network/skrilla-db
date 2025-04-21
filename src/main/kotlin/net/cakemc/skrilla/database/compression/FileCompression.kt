package net.cakemc.database.compression

/**
 * Abstract class that defines methods for compressing and decompressing byte arrays.
 *
 * This class serves as a base for implementing different file compression algorithms.
 * It defines two essential operations: compression (to reduce the size of data)
 * and decompression (to restore the original data).
 *
 * Concrete subclasses of this class should implement the actual compression and decompression
 * logic based on specific algorithms (e.g., GZIP, ZIP, etc.).
 *
 * @see java.util.zip.GZIPOutputStream
 * @see java.util.zip.GZIPInputStream
 */
abstract class FileCompression {

    /**
     * Compresses the given byte array.
     *
     * This method is responsible for reducing the size of the input byte array
     * using a specific compression algorithm.
     *
     * @param source The byte array to compress.
     * @return A new byte array containing the compressed data.
     */
    abstract fun compress(source: ByteArray): ByteArray

    /**
     * Decompresses the given byte array.
     *
     * This method restores the original data from the compressed byte array,
     * reversing the effect of the `compress()` method.
     *
     * @param source The compressed byte array to decompress.
     * @return A new byte array containing the decompressed data.
     */
    abstract fun decompress(source: ByteArray): ByteArray
}
