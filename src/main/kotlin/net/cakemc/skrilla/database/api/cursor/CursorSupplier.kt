package net.cakemc.database.cursor

/**
 * The [CursorSupplier] interface provides a mechanism to create a [Cursor] from a list of elements of type [E].
 * It is a generic interface that defines the contract for classes that supply a cursor based on a collection of elements.
 *
 * @param <E> the type of elements in the collection
 * @param <T> the type of the [Cursor] that will be created, which must be a subclass of [Cursor<E>]
 */
interface CursorSupplier<E, T : Cursor<E>> {

    /**
     * Creates a new [Cursor] based on the provided list of elements.
     *
     * @param elements the list of elements to create the cursor from
     * @return a new [Cursor] instance containing the provided elements
     */
    fun create(elements: List<E>): T
}
