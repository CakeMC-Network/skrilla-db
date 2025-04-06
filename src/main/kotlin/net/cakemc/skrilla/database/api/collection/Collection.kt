package net.cakemc.database.collection

import net.cakemc.database.api.Document
import net.cakemc.database.callbacks.*
import net.cakemc.database.cursor.Cursor
import net.cakemc.database.cursor.CursorSupplier
import net.cakemc.database.cursor.DocumentCursorSupplier
import net.cakemc.database.filter.Filter
import net.cakemc.database.filter.DocumentFilter

/**
 * The interface Collection.
 *
 * @param <T> the type parameter
</T> */
@Suppress("unused")
interface Collection<T> {
    /**
     * Multi Document async.
     *
     * @param filter         the filter
     * @param cursorSupplier the cursor supplier
     * @param callBack       the call back
     */
    // find
    fun multiDocumentAsync(filter: DocumentFilter, cursorSupplier: DocumentCursorSupplier, callBack: DocumentAsyncMultiCallBack)

    /**
     * Single Document async.
     *
     * @param supplier         the supplier
     * @param documentCallBack the document call back
     */
    fun singleDocumentAsync(supplier: DocumentFilter, documentCallBack: DocumentAsyncCallBack)

    /**
     * Multi async.
     *
     * @param filter         the filter
     * @param cursorSupplier the cursor supplier
     * @param callBack       the call back
     */
    fun multiAsync(filter: Filter<T>, cursorSupplier: CursorSupplier<T, Cursor<T>>, callBack: AsyncMultiCallBack<T>)

    /**
     * Single async.
     *
     * @param supplier         the supplier
     * @param documentCallBack the document call back
     */
    fun singleAsync(supplier: Filter<T>, documentCallBack: AsyncCallBack<T>)

    /**
     * Multi Document cursor.
     *
     * @param filter         the filter
     * @param cursorSupplier the cursor supplier
     * @return the cursor
     */
    fun multiDocument(filter: DocumentFilter, cursorSupplier: DocumentCursorSupplier): Cursor<Document>

    /**
     * Single Document Document.
     *
     * @param supplier the supplier
     * @return the Document
     */
    fun singleDocument(supplier: DocumentFilter): Document?

    /**
     * Multi cursor.
     *
     * @param filter         the filter
     * @param cursorSupplier the cursor supplier
     * @return the cursor
     */
    fun multi(filter: Filter<T>, cursorSupplier: CursorSupplier<T, Cursor<T>>): Cursor<T>

    /**
     * Single t.
     *
     * @param supplier the supplier
     * @return the t
     */
    fun single(supplier: Filter<T>): T?

    /**
     * Replace one Document.
     *
     * @param filter  the filter
     * @param element the element
     */
    // replace
    fun replaceOneDocument(filter: DocumentFilter, element: Document)

    /**
     * Replace one Document async.
     *
     * @param filter   the filter
     * @param element  the element
     * @param listener the listener
     */
    fun replaceOneDocumentAsync(filter: DocumentFilter, element: Document, listener: DatabaseListener)

    /**
     * Replace one.
     *
     * @param filter  the filter
     * @param element the element
     */
    fun replaceOne(filter: Filter<T>, element: T)

    /**
     * Replace one async.
     *
     * @param filter   the filter
     * @param element  the element
     * @param listener the listener
     */
    fun replaceOneAsync(filter: Filter<T>, element: T, listener: DatabaseListener)

    /**
     * Update one Document.
     *
     * @param filter  the filter
     * @param element the element
     */
    // update
    fun updateOneDocument(filter: DocumentFilter, element: Document)

    /**
     * Update one Document async.
     *
     * @param filter   the filter
     * @param element  the element
     * @param listener the listener
     */
    fun updateOneDocumentAsync(filter: DocumentFilter, element: Document, listener: DatabaseListener)

    /**
     * Update one.
     *
     * @param filter  the filter
     * @param element the element
     */
    fun updateOne(filter: Filter<T>, element: T)

    /**
     * Update one async.
     *
     * @param filter   the filter
     * @param element  the element
     * @param listener the listener
     */
    fun updateOneAsync(filter: Filter<T>, element: T, listener: DatabaseListener)

    /**
     * Delete one Document.
     *
     * @param element the element
     */
    // delete
    fun deleteOneDocument(element: Document)

    /**
     * Delete many Document.
     *
     * @param element the element
     */
    fun deleteManyDocument(vararg element: Document)

    /**
     * Delete one Document async.
     *
     * @param element  the element
     * @param listener the listener
     */
    fun deleteOneDocumentAsync(element: Document, listener: DatabaseListener)

    /**
     * Delete many Document async.
     *
     * @param listener the listener
     * @param element  the element
     */
    fun deleteManyDocumentAsync(listener: DatabaseListener, vararg element: Document)

    /**
     * Delete one.
     *
     * @param element the element
     */
    fun deleteOne(element: T)

    /**
     * Delete many.
     *
     * @param element the element
     */
    fun deleteMany(element: Array<T>)

    /**
     * Delete one async.
     *
     * @param element  the element
     * @param listener the listener
     */
    fun deleteOneAsync(element: T, listener: DatabaseListener)

    /**
     * Delete many async.
     *
     * @param element  the element
     * @param listener the listener
     */
    fun deleteManyAsync(element: Array<T>, listener: DatabaseListener)

    /**
     * Insert one Document.
     *
     * @param element the element
     */
    // insert
    fun insertOneDocument(element: Document)

    /**
     * Insert one Document async.
     *
     * @param element  the element
     * @param listener the listener
     */
    fun insertOneDocumentAsync(element: Document, listener: DatabaseListener)

    /**
     * Insert one.
     *
     * @param element the element
     */
    fun insertOne(element: T)

    /**
     * Insert one async.
     *
     * @param element  the element
     * @param listener the listener
     */
    fun insertOneAsync(element: T, listener: DatabaseListener)

    /**
     * Define one t.
     *
     * @return the t
     */
    // creation
    fun defineOne(): T

    /**
     * Define one peace Document.
     *
     * @return the Document
     */
    fun defineDocument(): Document


    /**
     * Gets id.
     *
     * @return the id
     */
    val id: Long

    /**
     * Gets name.
     *
     * @return the name
     */
    val name: String

    /**
     * Collect list.
     *
     * @return the list
     */
    fun collect(): List<Document>
}
