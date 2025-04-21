package net.cakemc.database.serial

import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.collection.Collection
import java.io.IOException
import java.util.function.Consumer

/**
 * Abstract base class for writing and serializing database components to binary format.
 *
 * Subclasses must implement logic for serializing entire collections and individual documents
 * into a byte array format suitable for storage or transmission.
 */
abstract class AbstractWrite {

    /**
     * Serializes a collection of `DatabaseRecord`s into a byte array.
     *
     * Each document in the collection is processed using the provided `consumer` function
     * before serialization, allowing for actions like mutation, tracking, or logging.
     *
     * @param collection The collection to serialize.
     * @param consumer A consumer that will be applied to each `Document` before serialization.
     * @return A byte array representing the serialized form of the collection.
     * @throws IOException If an I/O error occurs during serialization.
     */
    @Throws(IOException::class)
    abstract fun writeCollection(collection: Collection<DatabaseRecord>, consumer: Consumer<Document>): ByteArray

    /**
     * Serializes a single `Document` into a byte array.
     *
     * This method is used when saving or transmitting individual documents.
     *
     * @param document The document to serialize.
     * @return A byte array representing the serialized document.
     * @throws IOException If an I/O error occurs during serialization.
     */
    @Throws(IOException::class)
    abstract fun serializeDocument(document: Document): ByteArray
}
