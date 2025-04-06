package net.cakemc.skrilla.database.compression

import com.github.luben.zstd.Zstd
import net.cakemc.database.compression.FileCompression

class ZStdCompression: FileCompression() {
    /**
     * Compress byte [ ].
     *
     * @param source the source
     * @return the byte [ ]
     */
    override fun compress(source: ByteArray): ByteArray {
        return Zstd.compress(source)
    }

    /**
     * Decompress byte [ ].
     *
     * @param source the source
     * @return the byte [ ]
     */
    override fun decompress(source: ByteArray): ByteArray {
        return Zstd.decompress(source, source.size * 10)
    }
}