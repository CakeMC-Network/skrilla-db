package net.cakemc.database.collection

import net.cakemc.database.api.Document
import net.cakemc.database.callbacks.*
import net.cakemc.database.cursor.Cursor
import net.cakemc.database.cursor.CursorSupplier
import net.cakemc.database.cursor.DocumentCursorSupplier
import net.cakemc.database.filter.Filter
import net.cakemc.database.filter.DocumentFilter

/**
 * The Collection interface defines methods for interacting with a collection of elements
 * in a database-like structure. This includes operations like finding, inserting, updating,
 * and deleting elements, as well as managing documents asynchronously and synchronously.
 *
 * @param <T> the type of the elements stored in the collection
 */
@Suppress("unused")
interface Collection<T> {

    /**
     * Retrieves multiple documents asynchronously based on the given filter and cursor supplier.
     *
     * @param filter the filter that defines the conditions to match documents
     * @param cursorSupplier the cursor supplier used to create the cursor for the found documents
     * @param callBack the callback to handle the result or error of the operation
     */
    fun multiDocumentAsync(filter: DocumentFilter, cursorSupplier: DocumentCursorSupplier, callBack: DocumentAsyncMultiCallBack)

    /**
     * Retrieves a single document asynchronously based on the given filter.
     *
     * @param supplier the filter supplier that defines the conditions to match a single document
     * @param documentCallBack the callback to handle the result or error of the operation
     */
    fun singleDocumentAsync(supplier: DocumentFilter, documentCallBack: DocumentAsyncCallBack)

    /**
     * Retrieves multiple elements asynchronously based on the provided filter and cursor supplier.
     *
     * @param filter the filter that defines the conditions to match elements
     * @param cursorSupplier the cursor supplier used to create the cursor for the found elements
     * @param callBack the callback to handle the result or error of the operation
     */
    fun multiAsync(filter: Filter<T>, cursorSupplier: CursorSupplier<T, Cursor<T>>, callBack: AsyncMultiCallBack<T>)

    /**
     * Retrieves a single element asynchronously based on the given filter.
     *
     * @param supplier the filter supplier that defines the conditions to match a single element
     * @param documentCallBack the callback to handle the result or error of the operation
     */
    fun singleAsync(supplier: Filter<T>, documentCallBack: AsyncCallBack<T>)

    /**
     * Retrieves multiple documents synchronously based on the provided filter and cursor supplier.
     *
     * @param filter the filter that defines the conditions to match documents
     * @param cursorSupplier the cursor supplier used to create the cursor for the found documents
     * @return the cursor containing the matching documents
     */
    fun multiDocument(filter: DocumentFilter, cursorSupplier: DocumentCursorSupplier): Cursor<Document>

    /**
     * Retrieves a single document synchronously based on the provided filter.
     *
     * @param supplier the filter that defines the condition to match a single document
     * @return the matching document or null if no document is found
     */
    fun singleDocument(supplier: DocumentFilter): Document?

    /**
     * Finds a single document in the collection that matches the conditions specified by the provided filter.
     * The filter defines the criteria to find the document in the collection.
     * If multiple documents match the filter, only the first one encountered is returned.
     *
     * @param supplier the filter used to define the conditions that the document must match
     * @return the matching document, or null if no document is found that satisfies the filter
     */
    fun findDocument(supplier: DocumentFilter): Document?


    /**
     * Retrieves multiple elements synchronously based on the provided filter and cursor supplier.
     *
     * @param filter the filter that defines the conditions to match elements
     * @param cursorSupplier the cursor supplier used to create the cursor for the found elements
     * @return the cursor containing the matching elements
     */
    fun multi(filter: Filter<T>, cursorSupplier: CursorSupplier<T, Cursor<T>>): Cursor<T>

    /**
     * Retrieves a single element synchronously based on the provided filter.
     *
     * @param supplier the filter that defines the condition to match a single element
     * @return the matching element or null if no element is found
     */
    fun single(supplier: Filter<T>): T?

    /**
     * Replaces a single document based on the provided filter.
     *
     * @param filter the filter that defines the condition to match the document to be replaced
     * @param element the new document that will replace the existing one
     */
    fun replaceOneDocument(filter: DocumentFilter, element: Document)

    /**
     * Replaces a single document asynchronously based on the provided filter.
     *
     * @param filter the filter that defines the condition to match the document to be replaced
     * @param element the new document that will replace the existing one
     * @param listener the listener to handle success or failure of the operation
     */
    fun replaceOneDocumentAsync(filter: DocumentFilter, element: Document, listener: DatabaseListener)

    /**
     * Replaces a single element based on the provided filter.
     *
     * @param filter the filter that defines the condition to match the element to be replaced
     * @param element the new element that will replace the existing one
     */
    fun replaceOne(filter: Filter<T>, element: T)

    /**
     * Replaces a single element asynchronously based on the provided filter.
     *
     * @param filter the filter that defines the condition to match the element to be replaced
     * @param element the new element that will replace the existing one
     * @param listener the listener to handle success or failure of the operation
     */
    fun replaceOneAsync(filter: Filter<T>, element: T, listener: DatabaseListener)

    /**
     * Updates a single document based on the provided filter.
     *
     * @param filter the filter that defines the condition to match the document to be updated
     * @param element the document with updated data
     */
    fun updateOneDocument(filter: DocumentFilter, element: Document)

    /**
     * Updates a single document asynchronously based on the provided filter.
     *
     * @param filter the filter that defines the condition to match the document to be updated
     * @param element the document with updated data
     * @param listener the listener to handle success or failure of the operation
     */
    fun updateOneDocumentAsync(filter: DocumentFilter, element: Document, listener: DatabaseListener)

    /**
     * Updates a single element based on the provided filter.
     *
     * @param filter the filter that defines the condition to match the element to be updated
     * @param element the element with updated data
     */
    fun updateOne(filter: Filter<T>, element: T)

    /**
     * Updates a single element asynchronously based on the provided filter.
     *
     * @param filter the filter that defines the condition to match the element to be updated
     * @param element the element with updated data
     * @param listener the listener to handle success or failure of the operation
     */
    fun updateOneAsync(filter: Filter<T>, element: T, listener: DatabaseListener)

    /**
     * Deletes a single document.
     *
     * @param element the document to be deleted
     */
    fun deleteOneDocument(element: Document)

    /**
     * Deletes multiple documents.
     *
     * @param element the documents to be deleted
     */
    fun deleteManyDocument(vararg element: Document)

    /**
     * Deletes a single document asynchronously.
     *
     * @param element the document to be deleted
     * @param listener the listener to handle success or failure of the operation
     */
    fun deleteOneDocumentAsync(element: Document, listener: DatabaseListener)

    /**
     * Deletes multiple documents asynchronously.
     *
     * @param listener the listener to handle success or failure of the operation
     * @param element the documents to be deleted
     */
    fun deleteManyDocumentAsync(listener: DatabaseListener, vararg element: Document)

    /**
     * Deletes a single element.
     *
     * @param element the element to be deleted
     */
    fun deleteOne(element: T)

    /**
     * Deletes multiple elements.
     *
     * @param element the elements to be deleted
     */
    fun deleteMany(element: Array<T>)

    /**
     * Deletes a single element asynchronously.
     *
     * @param element the element to be deleted
     * @param listener the listener to handle success or failure of the operation
     */
    fun deleteOneAsync(element: T, listener: DatabaseListener)

    /**
     * Deletes multiple elements asynchronously.
     *
     * @param element the elements to be deleted
     * @param listener the listener to handle success or failure of the operation
     */
    fun deleteManyAsync(element: Array<T>, listener: DatabaseListener)

    /**
     * Inserts a single document.
     *
     * @param element the document to be inserted
     */
    fun insertOneDocument(element: Document)

    /**
     * Inserts a single document asynchronously.
     *
     * @param element the document to be inserted
     * @param listener the listener to handle success or failure of the operation
     */
    fun insertOneDocumentAsync(element: Document, listener: DatabaseListener)

    /**
     * Inserts a single element.
     *
     * @param element the element to be inserted
     */
    fun insertOne(element: T)

    /**
     * Inserts a single element asynchronously.
     *
     * @param element the element to be inserted
     * @param listener the listener to handle success or failure of the operation
     */
    fun insertOneAsync(element: T, listener: DatabaseListener)

    /**
     * Defines a new instance of the element type.
     *
     * @return a new instance of the element type
     */
    fun defineOne(): T

    /**
     * Defines a new document instance.
     *
     * @return a new document instance
     */
    fun defineDocument(): Document

    /**
     * Gets the ID of the collection.
     *
     * @return the ID of the collection
     */
    val id: Long

    /**
     * Gets the name of the collection.
     *
     * @return the name of the collection
     */
    val name: String

    /**
     * Collects and returns all documents from the collection.
     *
     * @return the list of all documents in the collection
     */
    fun collect(): List<Document>
}