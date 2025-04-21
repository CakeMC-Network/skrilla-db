package net.cakemc.database.cursor

import net.cakemc.database.filter.Filter

/**
 * The abstract [Cursor] class represents a cursor for iterating over a collection of elements of type [T].
 * It provides methods for limiting, sorting, and filtering the elements, as well as getting the index of an element.
 * Concrete implementations of this class should provide the logic for handling these operations on a specific data source.
 *
 * @param <T> the type of elements in the cursor
 */
abstract class Cursor<T> {

    /**
     * Limits the number of elements in the cursor to the specified [number].
     *
     * @param number the maximum number of elements to return from the cursor
     * @return a new [Cursor] instance with the specified limit
     */
    abstract fun limit(number: Int): Cursor<T>

    /**
     * Sorts the elements in the cursor based on the provided [comparator].
     *
     * @param comparator the [Comparator] used to sort the elements
     * @return a new [Cursor] instance with the elements sorted
     */
    abstract fun sort(comparator: Comparator<T>): Cursor<T>

    /**
     * Finds the index of the given [element] in the cursor.
     *
     * @param element the element to find the index of
     * @return the index of the element in the cursor, or -1 if not found
     */
    abstract fun index(element: T): Int

    /**
     * Finds the index of the first element that matches the provided [filter].
     *
     * @param filter the [Filter] to apply to find the matching element
     * @return the index of the element that matches the filter, or -1 if not found
     */
    abstract fun index(filter: Filter<T>): Int

    /**
     * Collects all the elements in the cursor into a [List].
     *
     * @return a list containing all the elements in the cursor
     */
    abstract fun collect(): List<T>
}
