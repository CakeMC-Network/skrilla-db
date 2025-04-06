package net.cakemc.database.compression

/**
 * The type File compression.
 */
abstract class FileCompression {
    /**
     * Compress byte [ ].
     *
     * @param source the source
     * @return the byte [ ]
     */
    abstract fun compress(source: ByteArray): ByteArray

    /**
     * Decompress byte [ ].
     *
     * @param source the source
     * @return the byte [ ]
     */
    abstract fun decompress(source: ByteArray): ByteArray
}
