package net.cakemc.skrilla.database.index

/**
 * Represents an index containing a list of entries, where each entry links a document's ID
 * to its corresponding file name and offset in storage.
 *
 * This class provides methods for retrieving the file name and offset of a document
 * based on its unique ID.
 *
 * @property entries A mutable list of [IndexEntry] objects, where each entry contains
 *                   information about a document's ID, file name, and offset.
 */
class Index(var entries: MutableList<IndexEntry>) {

    /**
     * Retrieves the file name for a document given its unique ID.
     *
     * This function looks through the list of entries and returns the file name of the first entry
     * that matches the given document ID.
     *
     * @param id The unique ID of the document to look up.
     * @return The file name associated with the document, or `null` if no entry is found for the given ID.
     */
    fun getFileNameForDocument(id: Long): String? {
        return entries.firstOrNull { it.documentId == id }?.fileName
    }

    /**
     * Retrieves the offset for a document given its unique ID.
     *
     * This function looks through the list of entries and returns the offset of the first entry
     * that matches the given document ID.
     *
     * @param id The unique ID of the document to look up.
     * @return The offset associated with the document, or `null` if no entry is found for the given ID.
     */
    fun getOffsetForDocument(id: Long): Long? {
        return entries.firstOrNull { it.documentId == id }?.offset
    }
}
