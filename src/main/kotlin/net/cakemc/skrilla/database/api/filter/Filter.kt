package net.cakemc.database.filter

/**
 * The interface Filter.
 *
 * @param <Document> the type parameter
</Document> */
interface Filter<Document> {
    /**
     * Matches boolean.
     *
     * @param document the document
     * @return the boolean
     */
    fun matches(document: Document): Boolean
}