package net.cakemc.skrilla.database

data class CollectionInfo(
    val createTime: Long,
    val lastWriteTime: Long,
    val lastReadTime: Long,
    val collectionId: Long
)
