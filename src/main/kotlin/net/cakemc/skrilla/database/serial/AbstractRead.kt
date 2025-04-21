package net.cakemc.database.serial

import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.collection.Collection
import java.io.DataInputStream
import java.io.IOException

/**
 * Abstract base class for reading serialized database components from binary data.
 *
 * Subclasses of `AbstractRead` must implement the deserialization logic for collections
 * and individual documents, allowing database components to be rehydrated from disk or memory.
 */
abstract class AbstractRead {

    /**
     * Deserializes a collection of `DatabaseRecord`s from a binary array.
     *
     * This method is typically used to load a complete collection from persisted data.
     *
     * @param data The byte array containing the serialized collection data.
     * @return A `Collection<DatabaseRecord>` instance reconstructed from the input.
     * @throws IOException If an I/O error occurs during deserialization.
     */
    @Throws(IOException::class)
    abstract fun read(data: ByteArray?): Collection<DatabaseRecord>

    /**
     * Deserializes a single `Document` from a byte array.
     *
     * This method is used to reconstruct a document from its serialized form.
     *
     * @param data The byte array containing the serialized document.
     * @return A `Document` instance deserialized from the byte array.
     * @throws IOException If an I/O error occurs during deserialization.
     */
    @Throws(IOException::class)
    abstract fun readElement(data: ByteArray): Document

    /**
     * Deserializes a single `Document` from a `DataInputStream`.
     *
     * This variant allows reading structured document data from a stream,
     * typically used when reading from large datasets or file streams.
     *
     * @param data The `DataInputStream` containing the serialized document.
     * @return A `Document` instance reconstructed from the input stream.
     * @throws IOException If an I/O error occurs during stream reading or decoding.
     */
    @Throws(IOException::class)
    abstract fun readElement(data: DataInputStream): Document
}
