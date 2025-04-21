package net.cakemc.database.filter

import net.cakemc.database.api.Document
import net.cakemc.database.callbacks.ConditionalConsumer

/**
 * The [Filters] object provides a collection of static utility methods to create various types of filters
 * for filtering [Document] objects. These filters can be used to evaluate whether a document matches
 * certain conditions, such as having a specific key-value pair, containing a key, or matching a specific id.
 * Additionally, it provides methods for combining multiple filters with logical operations like AND and OR.
 */
object Filters {

    /**
     * Creates a filter that checks if the value for the given [key] in the [Document] is equal to the specified [value].
     *
     * @param key the key to check in the document
     * @param value the value to compare against
     * @return a [DocumentFilter] that checks if the document contains the key with the specified value
     */
    @JvmStatic
    fun eq(key: String?, value: Any): DocumentFilter {
        return DocumentFilter { document: Document ->
            if (document.elements[key] == null) return@DocumentFilter false
            document.elements[key] == value
        }
    }

    /**
     * Creates a filter that checks if the given [Document] contains the specified [key].
     *
     * @param key the key to check for in the document
     * @return a [DocumentFilter] that checks if the document contains the key
     */
    @JvmStatic
    fun contains(key: String): DocumentFilter {
        return DocumentFilter { document: Document -> document.contains(key) }
    }

    /**
     * Creates a filter that checks if the given [Document] has the specified [id].
     *
     * @param id the id to match against
     * @return a [DocumentFilter] that checks if the document's id matches the given id
     */
    @JvmStatic
    fun id(id: Long): DocumentFilter {
        return DocumentFilter { document: Document -> document.id == id }
    }

    /**
     * Creates a filter that checks if the given [Document] has the specified [index].
     *
     * @param index the index to match against
     * @return a [DocumentFilter] that checks if the document's index matches the given index
     */
    @JvmStatic
    fun index(index: Int): DocumentFilter {
        return DocumentFilter { document: Document -> document.index == index }
    }

    /**
     * Creates a custom filter based on a key and a conditional consumer. This allows for more complex filtering conditions.
     * The consumer will evaluate the value associated with the key in the document.
     *
     * @param key the key to check in the document
     * @param consumer the [ConditionalConsumer] that will evaluate the value
     * @return a [DocumentFilter] that evaluates the custom condition
     */
    @JvmStatic @Suppress("UNCHECKED_CAST")
    fun <T> custom(key: String, consumer: ConditionalConsumer<T?>): DocumentFilter {
        return DocumentFilter { document: Document ->
            if (!document.contains(key)) return@DocumentFilter false
            val value = document.elements[key]
            consumer.expect(value as T?)
        }
    }

    /**
     * Combines multiple filters with a logical AND operation. A document will match if it satisfies all of the provided filters.
     *
     * @param filters the filters to combine
     * @return a [DocumentFilter] that represents the logical AND of all filters
     */
    @JvmStatic
    fun and(vararg filters: DocumentFilter): DocumentFilter {
        return DocumentFilter { document: Document ->
            filters.all { it.matches(document) }
        }
    }

    /**
     * Combines multiple filters with a logical OR operation. A document will match if it satisfies at least one of the provided filters.
     *
     * @param filters the filters to combine
     * @return a [DocumentFilter] that represents the logical OR of all filters
     */
    @JvmStatic
    fun or(vararg filters: DocumentFilter): DocumentFilter {
        return DocumentFilter { document: Document ->
            filters.any { it.matches(document) }
        }
    }
}
