package net.cakemc.skrilla.database.index

/**
 * Represents an entry in an index, linking a document's ID to its associated file name
 * and offset in storage.
 *
 * This data class is used to store the essential details required to locate a document
 * within a storage system, namely the document's unique ID, the file name where the
 * document is stored, and the offset within that file.
 *
 * @property documentId The unique ID of the document.
 * @property fileName The name of the file where the document is stored.
 * @property offset The offset in the file where the document starts.
 */
data class IndexEntry(
    val documentId: Long,
    val fileName: String,
    val offset: Long
)
