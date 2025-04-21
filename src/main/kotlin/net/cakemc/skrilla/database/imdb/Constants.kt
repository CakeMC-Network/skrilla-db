package net.cakemc.skrilla.database.imdb

/**
 * Contains constant values used throughout the database system.
 * These constants define various parameters such as key block size, maximum key size, and segment limits.
 */
internal object Constants {

    /**
     * The size of a key block in bytes.
     */
    const val keyBlockSize: Int = 4096

    /**
     * The maximum allowed size for a key in bytes.
     */
    const val maxKeySize: Int = 1000

    /**
     * The value used to mark the end of a block.
     */
    const val endOfBlock: Int = 0x8000

    /**
     * The bit value that indicates if the data is compressed.
     */
    const val compressedBit: Int = 0x8000

    /**
     * The maximum length of a prefix, calculated by XORing 0xFF with 0x80.
     */
    const val maxPrefixLen: Int = 0xFF xor 0x80

    /**
     * The maximum allowed length for compressed data.
     */
    const val maxCompressedLen: Int = 0xFF

    /**
     * The interval for key indexing, used to optimize search and retrieval.
     */
    const val keyIndexInterval: Int = 16

    /**
     * The maximum number of segments allowed in the database.
     */
    const val maxSegments: Int = 8
}
