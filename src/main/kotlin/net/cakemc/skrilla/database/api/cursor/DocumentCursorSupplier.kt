package net.cakemc.database.cursor

import net.cakemc.database.api.Document

/**
 * The [DocumentCursorSupplier] interface is a specific implementation of the [CursorSupplier] interface
 * designed for creating [Cursor] objects that handle [Document] elements.
 * It provides a way to generate a cursor that operates on a collection of [Document] objects.
 *
 * This functional interface allows the implementation of a single method to create a [Cursor] for [Document] elements.
 *
 * @see CursorSupplier
 * @see Cursor
 * @see Document
 */
fun interface DocumentCursorSupplier : CursorSupplier<Document, Cursor<Document>>
