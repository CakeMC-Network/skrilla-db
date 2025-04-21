package net.cakemc.database.cursor

import net.cakemc.database.api.Document
import net.cakemc.database.filter.Filter

/**
 * The `DefaultCursor` class is an implementation of the [Cursor] interface specifically designed for
 * handling a collection of [Document] objects. It provides various methods to interact with a list of
 * documents, including sorting, limiting the number of results, and finding the index of a specific document.
 *
 * It allows clients to query, sort, and retrieve documents efficiently within a collection.
 *
 * @param documents The list of [Document] objects this cursor will operate on.
 */
class DefaultCursor(
    /**
     * The list of [Document] objects that the cursor will manage and operate on.
     */
    var documents: List<Document>
) : Cursor<Document>() {

    /**
     * Limits the number of documents in the cursor to the specified [number].
     * This method restricts the cursor to only the first `number` of documents.
     *
     * @param number The maximum number of documents to retain in the cursor.
     * @return The updated cursor with the limited number of documents.
     */
    override fun limit(number: Int): Cursor<Document> {
        this.documents = documents.stream().limit(number.toLong()).toList()
        return this
    }

    /**
     * Sorts the documents in the cursor based on the provided [comparator].
     * This method uses the comparator to sort the list of documents in ascending or descending order.
     *
     * @param comparator The comparator used to compare and sort the documents.
     * @return The updated cursor with sorted documents.
     */
    override fun sort(comparator: Comparator<Document>): Cursor<Document> {
        documents.sortedWith(comparator)
        return this
    }

    /**
     * Finds the index of the specified [element] in the list of documents.
     * This method returns the index of the document if found, or -1 if the document is not in the list.
     *
     * @param element The [Document] whose index is to be found.
     * @return The index of the [Document] in the list, or -1 if not found.
     */
    override fun index(element: Document): Int {
        return documents.indexOf(element)
    }

    /**
     * Finds the index of the first document in the list that matches the provided [filter].
     * This method applies the filter to each document and returns the index of the first matching document.
     * If no document matches, it returns -1.
     *
     * @param filter The [Filter] to apply to the documents.
     * @return The index of the first document matching the filter, or -1 if no match is found.
     */
    override fun index(filter: Filter<Document>): Int {
        val single = documents.stream()
            .filter { document: Document -> filter.matches(document) }
            .findFirst()
            .orElse(null)

        if (single == null) return -1

        return documents.indexOf(single)
    }

    /**
     * Collects and returns the list of all documents in the cursor.
     * This method provides access to the full list of documents that the cursor is currently operating on.
     *
     * @return The list of [Document] objects contained in the cursor.
     */
    override fun collect(): List<Document> {
        return documents
    }
}
