package net.cakemc.database.collection

import net.cakemc.database.AbstractDatabase
import net.cakemc.database.callbacks.AsyncCallBack
import net.cakemc.database.callbacks.AsyncMultiCallBack
import net.cakemc.database.callbacks.DatabaseListener
import net.cakemc.database.cursor.Cursor
import net.cakemc.database.cursor.CursorSupplier
import net.cakemc.database.filter.Filter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

/**
 * The `AbstractCollection` class represents an abstract implementation of a collection within the database.
 * It allows for operations on a list of elements of type `T`, including querying, inserting, updating,
 * replacing, and deleting elements. This class also supports both synchronous and asynchronous operations
 * with callbacks for various database actions.
 *
 * @param T The type of the elements contained in the collection.
 * @param elements The list of elements in the collection.
 * @param id The unique identifier for the collection.
 * @param name The name of the collection.
 */
abstract class AbstractCollection<T>(
    /**
     * A mutable list of elements contained in the collection.
     */
    @JvmField val elements: MutableList<T>,

    /**
     * The unique identifier for the collection.
     */
    override val id: Long,

    /**
     * The name of the collection.
     */
    override val name: String
) : Collection<T> {

    /**
     * Asynchronously finds multiple elements that match the provided [filter] and processes them using
     * the provided [cursorSupplier] and [callBack].
     *
     * @param filter The [Filter] to apply to the elements.
     * @param cursorSupplier A supplier that provides a cursor to iterate over the filtered elements.
     * @param callBack The callback to handle the result.
     */
    override fun multiAsync(
        filter: Filter<T>,
        cursorSupplier: CursorSupplier<T, Cursor<T>>,
        callBack: AsyncMultiCallBack<T>
    ) {
        try {
            val completableFuture = CompletableFuture.runAsync(
                {
                    callBack.acceptFound(cursorSupplier.create(
                        elements.stream()
                            .filter { document: T -> filter.matches(document) }
                            .toList()))
                }, AbstractDatabase.EXECUTOR
            )
            completableFuture.get()
        } catch (exception: InterruptedException) {
            callBack.acceptException(exception)
        } catch (exception: ExecutionException) {
            callBack.acceptException(exception)
        }
    }

    /**
     * Asynchronously finds a single element that matches the provided [supplier] filter and processes it
     * using the provided [documentCallBack].
     *
     * @param supplier The [Filter] to apply to the elements.
     * @param documentCallBack The callback to handle the result.
     */
    override fun singleAsync(supplier: Filter<T>, documentCallBack: AsyncCallBack<T>) {
        try {
            val completableFuture = CompletableFuture.runAsync(
                {
                    val element = elements.stream().filter { document: T -> supplier.matches(document) }
                        .findFirst()
                        .orElse(null)
                    if (element == null) {
                        documentCallBack.acceptNotFound()
                        return@runAsync
                    }
                    documentCallBack.acceptFound(element)
                }, AbstractDatabase.EXECUTOR
            )
            completableFuture.get()
        } catch (exception: InterruptedException) {
            documentCallBack.acceptException(exception)
        } catch (exception: ExecutionException) {
            documentCallBack.acceptException(exception)
        }
    }

    /**
     * Finds and returns a cursor containing all elements that match the provided [filter].
     *
     * @param filter The [Filter] to apply to the elements.
     * @param cursorSupplier A supplier that provides a cursor to iterate over the filtered elements.
     * @return A [Cursor] containing the elements that match the filter.
     */
    override fun multi(filter: Filter<T>, cursorSupplier: CursorSupplier<T, Cursor<T>>): Cursor<T> {
        return cursorSupplier.create(elements.stream().filter { document: T -> filter.matches(document) }.toList())
    }

    /**
     * Finds and returns a single element that matches the provided [supplier] filter.
     *
     * @param supplier The [Filter] to apply to the elements.
     * @return The first element that matches the filter, or null if no match is found.
     */
    override fun single(supplier: Filter<T>): T {
        return elements.stream().filter { document: T -> supplier.matches(document) }.findFirst().orElse(null)
    }

    /**
     * Replaces a single element that matches the provided [filter] with a new [element].
     *
     * @param filter The [Filter] to find the element to replace.
     * @param element The new element to insert in place of the matched element.
     */
    override fun replaceOne(filter: Filter<T>, element: T) {
        elements.removeIf { document: T -> filter.matches(document) }
        elements.add(element)
    }

    /**
     * Asynchronously replaces a single element that matches the provided [filter] with a new [element],
     * and notifies the provided [listener] of success or failure.
     *
     * @param filter The [Filter] to find the element to replace.
     * @param element The new element to insert in place of the matched element.
     * @param listener The listener to handle the result of the operation.
     */
    override fun replaceOneAsync(filter: Filter<T>, element: T, listener: DatabaseListener) {
        try {
            val completableFuture = CompletableFuture.runAsync(
                {
                    if (elements.removeIf { document: T -> filter.matches(document) }) {
                        elements.add(element)
                        listener.success()
                    } else listener.accept(DatabaseListener.State.FAILED, null)
                }, AbstractDatabase.EXECUTOR
            )
            completableFuture.get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Updates a single element that matches the provided [filter] with a new [element].
     * Currently, this method doesn't implement the update logic and is a placeholder for future development.
     *
     * @param filter The [Filter] to find the element to update.
     * @param element The new element to replace the matched element.
     */
    override fun updateOne(filter: Filter<T>, element: T) {
        // todo update fields in filter
    }

    /**
     * Asynchronously updates a single element that matches the provided [filter] with a new [element],
     * and notifies the provided [listener] of success or failure.
     *
     * @param filter The [Filter] to find the element to update.
     * @param element The new element to replace the matched element.
     * @param listener The listener to handle the result of the update operation.
     */
    override fun updateOneAsync(filter: Filter<T>, element: T, listener: DatabaseListener) {
        try {
            val completableFuture = CompletableFuture.runAsync(
                {}, AbstractDatabase.EXECUTOR
            )
            completableFuture.get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Deletes a single element from the collection.
     *
     * @param element The element to delete from the collection.
     */
    override fun deleteOne(element: T) {
        elements.remove(element)
    }

    /**
     * Deletes multiple elements from the collection.
     *
     * @param element The array of elements to delete.
     */
    override fun deleteMany(element: Array<T>) {
        for (current in element) {
            elements.remove(current)
        }
    }

    /**
     * Asynchronously deletes a single element from the collection and notifies the provided [listener] of success or failure.
     *
     * @param element The element to delete from the collection.
     * @param listener The listener to handle the result of the deletion.
     */
    override fun deleteOneAsync(element: T, listener: DatabaseListener) {
        try {
            val completableFuture = CompletableFuture.runAsync(
                {
                    if (elements.remove(element)) listener.success()
                    else listener.accept(DatabaseListener.State.FAILED, null)
                }, AbstractDatabase.EXECUTOR
            )
            completableFuture.get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Asynchronously deletes multiple elements from the collection and notifies the provided [listener] of success or failure.
     *
     * @param element The array of elements to delete.
     * @param listener The listener to handle the result of the deletion.
     */
    override fun deleteManyAsync(element: Array<T>, listener: DatabaseListener) {
        try {
            val completableFuture = CompletableFuture.runAsync(
                {
                    for (current in elements) {
                        elements.remove(current)
                    }
                    listener.success()
                }, AbstractDatabase.EXECUTOR
            )
            completableFuture.get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Inserts a single element into the collection.
     *
     * @param element The element to insert.
     */
    override fun insertOne(element: T) {
        elements.add(element)
    }

    /**
     * Asynchronously inserts a single element into the collection and notifies the provided [listener] of success.
     *
     * @param element The element to insert.
     * @param listener The listener to handle the result of the insertion.
     */
    override fun insertOneAsync(element: T, listener: DatabaseListener) {
        try {
            val completableFuture = CompletableFuture.runAsync(
                {
                    elements.add(element)
                    listener.success()
                }, AbstractDatabase.EXECUTOR
            )
            completableFuture.get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Returns a string representation of the collection, including its elements, id, and name.
     *
     * @return A string representation of the collection.
     */
    override fun toString(): String {
        return "AbstractCollection{" +
                "elements=" + elements +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}'
    }
}
