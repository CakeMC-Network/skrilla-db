package net.cakemc.skrilla.database.api

/**
 * Represents metadata information for a database collection. This class holds the
 * timestamps related to the creation, last write, and last read of the collection,
 * as well as the unique identifier for the collection.
 *
 * @property createTime The timestamp (in milliseconds) when the collection was created.
 * @property lastWriteTime The timestamp (in milliseconds) of the last write operation performed on the collection.
 * @property lastReadTime The timestamp (in milliseconds) of the last read operation performed on the collection.
 * @property collectionId The unique identifier for the collection.
 */
data class CollectionInfo(
    /**
     * The timestamp (in milliseconds) when the collection was created.
     */
    val createTime: Long,

    /**
     * The timestamp (in milliseconds) of the last write operation on the collection.
     */
    val lastWriteTime: Long,

    /**
     * The timestamp (in milliseconds) of the last read operation on the collection.
     */
    val lastReadTime: Long,

    /**
     * The unique identifier for the collection.
     */
    val collectionId: Long
)
