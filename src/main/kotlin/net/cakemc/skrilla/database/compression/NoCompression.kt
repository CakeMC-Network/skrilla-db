package net.cakemc.skrilla.database.compression

import net.cakemc.database.compression.FileCompression

class NoCompression: FileCompression() {
    /**
     * Compress byte [ ].
     *
     * @param source the source
     * @return the byte [ ]
     */
    override fun compress(source: ByteArray): ByteArray {
        return source
    }

    /**
     * Decompress byte [ ].
     *
     * @param source the source
     * @return the byte [ ]
     */
    override fun decompress(source: ByteArray): ByteArray {
        return source
    }
}