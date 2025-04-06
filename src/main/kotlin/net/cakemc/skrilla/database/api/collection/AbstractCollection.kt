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
 * The type Abstract collection.
 *
 * @param <T> the type parameter
</T> */
abstract class AbstractCollection<T>
/**
 * Instantiates a new Abstract collection.
 *
 * @param elements the elements
 * @param id       the id
 * @param name     the name
 */(
    @JvmField val elements: MutableList<T>, override val id: Long,
    override val name: String
) : Collection<T> {

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

    override fun multi(filter: Filter<T>, cursorSupplier: CursorSupplier<T, Cursor<T>>): Cursor<T> {
        return cursorSupplier.create(elements.stream().filter { document: T -> filter.matches(document) }.toList())
    }

    override fun single(supplier: Filter<T>): T {
        return elements.stream().filter { document: T -> supplier.matches(document) }.findFirst().orElse(null)
    }

    override fun replaceOne(filter: Filter<T>, element: T) {
        elements.removeIf { document: T -> filter.matches(document) }
        elements.add(element)
    }

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

    override fun updateOne(filter: Filter<T>, element: T) {
        // todo update fields in filter
    }

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

    override fun deleteOne(element: T) {
        elements.remove(element)
    }

    override fun deleteMany(element: Array<T>) {
        for (current in element) {
            elements.remove(current)
        }
    }

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

    override fun insertOne(element: T) {
        elements.add(element)
    }

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

    override fun toString(): String {
        return "AbstractCollection{" +
                "elements=" + elements +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}'
    }
}
