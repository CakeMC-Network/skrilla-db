package net.cakemc.database.collection

import net.cakemc.database.AbstractDatabase
import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.callbacks.DatabaseListener
import net.cakemc.database.callbacks.DocumentAsyncCallBack
import net.cakemc.database.callbacks.DocumentAsyncMultiCallBack
import net.cakemc.database.cursor.Cursor
import net.cakemc.database.cursor.DocumentCursorSupplier
import net.cakemc.database.filter.DocumentFilter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ThreadLocalRandom

/**
 * Implementation of a collection that handles `Document` objects. It provides methods for inserting, updating, deleting,
 * and retrieving documents asynchronously or synchronously.
 *
 * This class extends `AbstractCollection` and is used to manage a list of `Document` instances in a collection.
 * The collection provides functionality for filtering, sorting, and performing CRUD (Create, Read, Update, Delete) operations
 * on documents stored within it.
 *
 * @param elements a mutable list of `DatabaseRecord` objects representing the elements in the collection
 * @param id the unique identifier for the collection
 * @param name the name of the collection
 */
open class DocumentCollection(
    elements: MutableList<DatabaseRecord>,
    id: Long,
    name: String
) : AbstractCollection<DatabaseRecord>(elements, id, name) {

    /**
     * Asynchronously retrieves multiple documents that match the specified filter and passes the results to the provided callback.
     *
     * @param filter the `DocumentFilter` used to filter the documents in the collection
     * @param cursorSupplier the supplier that creates a cursor from the filtered results
     * @param callBack the callback that will receive the matching documents
     */
    override fun multiDocumentAsync(filter: DocumentFilter, cursorSupplier: DocumentCursorSupplier, callBack: DocumentAsyncMultiCallBack) {
        try {
            CompletableFuture.runAsync({
                val matchedElements = elements
                    .filter { filter.matches(it as Document) }
                    .map { it as Document }

                callBack.acceptFound(cursorSupplier.create(matchedElements))
            }, AbstractDatabase.EXECUTOR).get()
        } catch (exception: InterruptedException) {
            callBack.acceptException(exception)
        } catch (exception: ExecutionException) {
            callBack.acceptException(exception)
        }
    }

    /**
     * Asynchronously retrieves a single document that matches the provided filter and passes the result to the provided callback.
     *
     * @param supplier the `DocumentFilter` used to filter the document in the collection
     * @param documentCallBack the callback that will receive the found document or a "not found" result
     */
    override fun singleDocumentAsync(supplier: DocumentFilter, documentCallBack: DocumentAsyncCallBack) {
        try {
            CompletableFuture.runAsync({
                val element = elements
                    .filterIsInstance<Document>()
                    .find { supplier.matches(it) }

                if (element == null) {
                    documentCallBack.acceptNotFound()
                } else {
                    documentCallBack.acceptFound(element)
                }
            }, AbstractDatabase.EXECUTOR).get()
        } catch (exception: InterruptedException) {
            documentCallBack.acceptException(exception)
        } catch (exception: ExecutionException) {
            documentCallBack.acceptException(exception)
        }
    }

    /**
     * Retrieves multiple documents that match the specified filter and returns them as a cursor.
     *
     * @param filter the `DocumentFilter` used to filter the documents in the collection
     * @param cursorSupplier the supplier that creates a cursor from the filtered results
     * @return a `Cursor<Document>` containing the matching documents
     */
    override fun multiDocument(filter: DocumentFilter, cursorSupplier: DocumentCursorSupplier): Cursor<Document> {
        val matchedElements = elements
            .filterIsInstance<Document>()
            .filter { filter.matches(it) }
        return cursorSupplier.create(matchedElements)
    }

    /**
     * Retrieves a single document that matches the provided filter.
     *
     * @param supplier the `DocumentFilter` used to filter the document in the collection
     * @return the matching `Document`, or `null` if no match is found
     */
    override fun singleDocument(supplier: DocumentFilter): Document? {
        return elements.filterIsInstance<Document>().find { supplier.matches(it) }
    }

    /**
     * Finds a single document in the collection that matches the conditions specified by the provided filter.
     *
     * @param supplier the `DocumentFilter` that defines the conditions the document must meet
     * @return the matching document, or `null` if no document is found that satisfies the filter
     */
    override fun findDocument(supplier: DocumentFilter): Document? {
        return elements.filterIsInstance<Document>().find { supplier.matches(it) }
    }

    /**
     * Replaces the first document that matches the specified filter with the provided document.
     *
     * @param filter the `DocumentFilter` used to find the document to replace
     * @param element the `Document` to insert into the collection
     */
    override fun replaceOneDocument(filter: DocumentFilter, element: Document) {
        elements.removeIf { filter.matches(it as Document) }
        elements.add(element)
    }

    /**
     * Asynchronously replaces the first document that matches the specified filter with the provided document.
     *
     * @param filter the `DocumentFilter` used to find the document to replace
     * @param element the `Document` to insert into the collection
     * @param listener the `DatabaseListener` that will be notified of the result
     */
    override fun replaceOneDocumentAsync(filter: DocumentFilter, element: Document, listener: DatabaseListener) {
        try {
            CompletableFuture.runAsync({
                if (elements.removeIf { filter.matches(it as Document) }) {
                    elements.add(element)
                    listener.success()
                } else {
                    listener.accept(DatabaseListener.State.FAILED, null)
                }
            }, AbstractDatabase.EXECUTOR).get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Updates the first document that matches the specified filter with the values from the provided document.
     *
     * @param filter the `DocumentFilter` used to find the document to update
     * @param element the `Document` containing the new values for the document
     */
    override fun updateOneDocument(filter: DocumentFilter, element: Document) {
        val current = this.elements.stream().filter { filter.matches(it as Document) }.findFirst()

        if (current.isEmpty) {
            this.insertOneDocument(element)
            return
        }

        val document: Document = current.get() as Document;

        for (containing in element.elements) {
            val key = containing.key;

            if (document.contains(key)) {
                document.remove(key)
                document.set(containing.key, containing.value)
                continue
            }

            document.set(containing.key, containing.value)
        }

        elements.removeIf { filter.matches(it as Document) }
        elements.add(document)
    }

    /**
     * Asynchronously updates the first document that matches the specified filter with the values from the provided document.
     *
     * @param filter the `DocumentFilter` used to find the document to update
     * @param element the `Document` containing the new values for the document
     * @param listener the `DatabaseListener` that will be notified of the result
     */
    override fun updateOneDocumentAsync(filter: DocumentFilter, element: Document, listener: DatabaseListener) {
        try {
            CompletableFuture.runAsync({
                updateOneDocument(filter, element)
            }, AbstractDatabase.EXECUTOR).get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Deletes a single document from the collection.
     *
     * @param element the `Document` to delete
     */
    override fun deleteOneDocument(element: Document) {
        elements.remove(element)
    }

    /**
     * Deletes multiple documents from the collection.
     *
     * @param element the documents to delete
     */
    override fun deleteManyDocument(vararg element: Document) {
        element.forEach { elements.remove(it) }
    }

    /**
     * Asynchronously deletes a single document from the collection.
     *
     * @param element the `Document` to delete
     * @param listener the `DatabaseListener` that will be notified of the result
     */
    override fun deleteOneDocumentAsync(element: Document, listener: DatabaseListener) {
        try {
            CompletableFuture.runAsync({
                if (elements.remove(element)) {
                    listener.success()
                } else {
                    listener.accept(DatabaseListener.State.FAILED, null)
                }
            }, AbstractDatabase.EXECUTOR).get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Asynchronously deletes multiple documents from the collection.
     *
     * @param listener the `DatabaseListener` that will be notified of the result
     * @param element the documents to delete
     */
    override fun deleteManyDocumentAsync(listener: DatabaseListener, vararg element: Document) {
        try {
            CompletableFuture.runAsync({
                element.forEach { elements.remove(it) }
                listener.success()
            }, AbstractDatabase.EXECUTOR).get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Inserts a single document into the collection.
     *
     * @param element the `Document` to insert
     */
    override fun insertOneDocument(element: Document) {
        elements.add(element)
    }

    /**
     * Asynchronously inserts a single document into the collection.
     *
     * @param element the `Document` to insert
     * @param listener the `DatabaseListener` that will be notified of the result
     */
    override fun insertOneDocumentAsync(element: Document, listener: DatabaseListener) {
        try {
            CompletableFuture.runAsync({
                elements.add(element)
                listener.success()
            }, AbstractDatabase.EXECUTOR).get()
        } catch (exception: InterruptedException) {
            listener.exception(exception)
        } catch (exception: ExecutionException) {
            listener.exception(exception)
        }
    }

    /**
     * Defines a new record (not implemented for this collection).
     *
     * @throws UnsupportedOperationException always thrown
     */
    override fun defineOne(): DatabaseRecord {
        throw UnsupportedOperationException("Not implemented!")
    }

    /**
     * Defines a new document with a unique index and id.
     *
     * @return a newly created `Document`
     */
    override fun defineDocument(): Document {
        val index = elements.size + 1
        val id = nextFreeId()
        val flags = 0

        return Document(index, flags, id)
    }

    /**
     * Generates a unique id for the next document.
     *
     * @return a unique id
     */
    private fun nextFreeId(): Long {
        val current = ThreadLocalRandom.current().nextLong()
        return if (elements.any { it.id == current }) nextFreeId() else current
    }

    /**
     * Collects all documents from the collection.
     *
     * @return a list of all `Document` objects in the collection
     */
    override fun collect(): List<Document> {
        return elements.filterIsInstance<Document>()
    }
}
