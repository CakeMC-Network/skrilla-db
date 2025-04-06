package net.cakemc.database.cursor

import net.cakemc.database.api.Document
import net.cakemc.database.filter.Filter

/**
 * The type Default cursor.
 */
class DefaultCursor
/**
 * Instantiates a new Default cursor.
 *
 * @param documents the pieces
 */(
    /**
     * Gets pieces.
     *
     * @return the pieces
     */
    var documents: List<Document>

) : Cursor<Document>() {

    override fun limit(number: Int): Cursor<Document> {
        this.documents = documents.stream().limit(number.toLong()).toList()
        return this
    }

    override fun sort(comparator: Comparator<Document>): Cursor<Document> {
        documents.sortedWith(comparator)
        return this
    }

    override fun index(element: Document): Int {
        return documents.indexOf(element)
    }

    override fun index(filter: Filter<Document>): Int {
        val single = documents.stream()
            .filter { document: Document -> filter.matches(document) }
            .findFirst()
            .orElse(null)

        if (single == null) return -1

        return documents.indexOf(single)
    }

    override fun collect(): List<Document> {
        return documents
    }
}
