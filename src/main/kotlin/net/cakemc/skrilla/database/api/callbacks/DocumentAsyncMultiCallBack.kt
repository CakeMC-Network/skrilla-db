package net.cakemc.database.callbacks

import net.cakemc.database.api.Document

/**
 * The interface for handling asynchronous callback operations that return multiple `Document` objects.
 * This interface extends `AsyncMultiCallBack<Document>`, allowing for custom handling of a collection of `Document` objects.
 *
 * It is typically used when querying a collection of documents asynchronously, and it provides a callback method
 * that accepts the results once the query completes.
 *
 * The callback will provide the matched `Document` objects, usually in the form of a cursor or collection of documents.
 */
interface DocumentAsyncMultiCallBack : AsyncMultiCallBack<Document>
