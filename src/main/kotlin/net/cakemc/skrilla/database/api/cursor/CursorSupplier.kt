package net.cakemc.database.cursor

/**
 * The interface Cursor supplier.
 *
 * @param <E> the type parameter
 * @param <T> the type parameter
</T></E> */
interface CursorSupplier<E, T : Cursor<E>> {
    /**
     * Create t.
     *
     * @param elements the elements
     * @return the t
     */
    fun create(elements: List<E>): T
}
