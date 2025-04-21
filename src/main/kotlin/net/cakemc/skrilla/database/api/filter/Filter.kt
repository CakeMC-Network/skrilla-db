package net.cakemc.database.filter

/**
 * The [Filter] interface represents a general contract for defining filtering criteria
 * that can be applied to objects of type [Document]. Implementing classes or lambda expressions
 * that define this interface will have the ability to evaluate whether a given [Document]
 * matches a specific condition.
 *
 * This is a generic interface that can be used with any type of object (in this case, [Document]),
 * providing flexibility in filtering various kinds of objects based on custom logic.
 *
 * Example usage:
 * ```kotlin
 * val filter: Filter<Document> = object : Filter<Document> {
 *     override fun matches(document: Document): Boolean {
 *         return document.getInt("age") > 18
 *     }
 * }
 * ```
 * Or using a lambda expression:
 * ```kotlin
 * val filter: Filter<Document> = Filter { document -> document.getInt("age") > 18 }
 * ```
 *
 * @param Document the type of the object that this filter will operate on
 */
interface Filter<Document> {
    /**
     * Evaluates whether the provided [document] matches the filtering condition.
     *
     * @param document the object of type [Document] to be checked against the filter condition
     * @return `true` if the document matches the filter condition, `false` otherwise
     */
    fun matches(document: Document): Boolean
}
