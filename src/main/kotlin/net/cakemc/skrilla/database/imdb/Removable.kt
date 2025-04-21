package net.cakemc.skrilla.database.imdb

import java.io.IOException

/**
 * An interface representing an object that can be removed.
 * Classes implementing this interface must define the logic for removing an object.
 */
interface Removable {

    /**
     * Removes the object.
     * This method may throw an [IOException] if an error occurs during the removal process.
     *
     * @throws IOException If there is an error during the removal.
     */
    @Throws(IOException::class)
    fun remove()
}
