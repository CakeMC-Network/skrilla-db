package net.cakemc.database.cursor

import net.cakemc.database.api.Document

/**
 * The interface Piece cursor supplier.
 */
fun interface DocumentCursorSupplier : CursorSupplier<Document, Cursor<Document>>
