package net.cakemc.database.serial

import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.collection.Collection
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * The type Abstract read.
 */
abstract class AbstractRead {
    /**
     * Read collection.
     *
     * @param data the data
     * @return the collection
     * @throws IOException the io exception
     */
    @Throws(IOException::class)
    abstract fun read(data: ByteArray?): Collection<DatabaseRecord>
    @Throws(IOException::class)
    abstract fun readElement(data: ByteArray): Document
    @Throws(IOException::class)
    abstract fun readElement(data: DataInputStream): Document
}
