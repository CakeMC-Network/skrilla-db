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
 * Document collection implementation.
 */
open class DocumentCollection(
    elements: MutableList<DatabaseRecord>,
    id: Long,
    name: String
) : AbstractCollection<DatabaseRecord>(elements, id, name) {

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

    override fun multiDocument(filter: DocumentFilter, cursorSupplier: DocumentCursorSupplier): Cursor<Document> {
        val matchedElements = elements
            .filterIsInstance<Document>()
            .filter { filter.matches(it) }
        return cursorSupplier.create(matchedElements)
    }

    override fun singleDocument(supplier: DocumentFilter): Document? {
        return elements.filterIsInstance<Document>().find { supplier.matches(it) }
    }

    override fun replaceOneDocument(filter: DocumentFilter, element: Document) {
        elements.removeIf { filter.matches(it as Document) }
        elements.add(element)
    }

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

    override fun updateOneDocument(filter: DocumentFilter, other: Document) {
        val current = this.elements.stream().filter { filter.matches(it as Document) }.findFirst()

        if (current.isEmpty) {
            this.insertOneDocument(other)
            return
        }

        val document: Document = current.get() as Document;

        for (containing in other.elements) {
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

    override fun deleteOneDocument(element: Document) {
        elements.remove(element)
    }

    override fun deleteManyDocument(vararg element: Document) {
        element.forEach { elements.remove(it) }
    }

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

    override fun insertOneDocument(element: Document) {
        elements.add(element)
    }

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

    override fun defineOne(): DatabaseRecord {
        throw UnsupportedOperationException("Not implemented!")
    }

    override fun defineDocument(): Document {
        val index = elements.size + 1
        val id = nextFreeId()
        val flags = 0

        return Document(index, flags, id)
    }

    private fun nextFreeId(): Long {
        val current = ThreadLocalRandom.current().nextLong()
        return if (elements.any { it.id == current }) nextFreeId() else current
    }

    override fun collect(): List<Document> {
        return elements.filterIsInstance<Document>()
    }
}
