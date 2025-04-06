package net.cakemc.database.filter

import net.cakemc.database.api.Document
import net.cakemc.database.callbacks.ConditionalConsumer

/**
 * The type Filters.
 */
object Filters {
    /**
     * Eq piece piece filter.
     *
     * @param key   the key
     * @param value the value
     * @return the piece filter
     */
    @JvmStatic
    fun eq(key: String?, value: Any): DocumentFilter {
        return DocumentFilter { document: Document ->
            if (document.elements[key] == null) return@DocumentFilter false
            document.elements[key] == value
        }
    }

    /**
     * Contains piece filter.
     *
     * @param key the key
     * @return the piece filter
     */
    @JvmStatic
    fun contains(key: String): DocumentFilter {
        return DocumentFilter { document: Document -> document.contains(key) }
    }

    /**
     * Id piece filter.
     *
     * @param id the id
     * @return the piece filter
     */
    @JvmStatic
    fun id(id: Long): DocumentFilter {
        return DocumentFilter { document: Document -> document.id == id }
    }

    /**
     * Index piece filter.
     *
     * @param index the index
     * @return the piece filter
     */
    @JvmStatic
    fun index(index: Int): DocumentFilter {
        return DocumentFilter { document: Document -> document.index == index }
    }

    /**
     * Custom piece filter.
     *
     * @param <T>      the type parameter
     * @param key      the key
     * @param consumer the consumer
     * @return the piece filter
    </T> */
    @JvmStatic
    fun <T> custom(key: String, consumer: ConditionalConsumer<T?>): DocumentFilter {
        return DocumentFilter { document: Document ->
            if (!document.contains(key)) return@DocumentFilter false
            val value = document.elements[key]
            consumer.expect(value as T?)
        }
    }

    /**
     * And piece filter
     *
     * @param filters the filters
     * @return the combined piece filter
     */
    @JvmStatic
    fun and(vararg filters: DocumentFilter): DocumentFilter {
        return DocumentFilter { document: Document ->
            filters.all { it.matches(document) }
        }
    }

    /**
     * Or piece filter
     *
     * @param filters the filters
     * @return the combined piece filter
     */
    @JvmStatic
    fun or(vararg filters: DocumentFilter): DocumentFilter {
        return DocumentFilter { document: Document ->
            filters.any { it.matches(document) }
        }
    }
}
