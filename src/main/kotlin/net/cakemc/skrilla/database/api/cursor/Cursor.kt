package net.cakemc.database.cursor

import net.cakemc.database.filter.Filter

/**
 * The type Cursor.
 *
 * @param <T> the type parameter
</T> */
abstract class Cursor<T> {
    /**
     * Limit cursor.
     *
     * @param number the number
     * @return the cursor
     */
    abstract fun limit(number: Int): Cursor<T>

    /**
     * Sort cursor.
     *
     * @param comparator the comparator
     * @return the cursor
     */
    abstract fun sort(comparator: Comparator<T>): Cursor<T>

    /**
     * Index int.
     *
     * @param element the element
     * @return the int
     */
    abstract fun index(element: T): Int

    /**
     * Index int.
     *
     * @param filter the filter
     * @return the int
     */
    abstract fun index(filter: Filter<T>): Int

    /**
     * Collect list.
     *
     * @return the list
     */
    abstract fun collect(): List<T>
}
