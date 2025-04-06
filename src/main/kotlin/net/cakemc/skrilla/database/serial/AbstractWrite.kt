package net.cakemc.database.serial

import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.collection.Collection
import java.io.IOException
import java.util.function.Consumer

/**
 * The type Abstract write.
 */
abstract class AbstractWrite {
    /**
     * Write collection byte [ ].
     *
     * @param collection the collection
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    @Throws(IOException::class)
    abstract fun writeCollection(collection: Collection<DatabaseRecord>, consumer: Consumer<Document>): ByteArray
    @Throws(IOException::class)
    abstract fun serializeDocument(document: Document): ByteArray
}
