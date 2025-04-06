package net.cakemc.database.callbacks

import net.cakemc.database.api.Document

/**
 * The interface Piece async call back.
 */
fun interface DocumentAsyncCallBack : AsyncCallBack<Document?> {

    override fun acceptException(exception: Exception) {
        super.acceptException(exception)
    }
}
