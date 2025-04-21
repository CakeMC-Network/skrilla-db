package net.cakemc.database.serial.impl

import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.collection.Collection
import net.cakemc.database.serial.AbstractWrite
import java.io.*
import java.util.*
import java.util.function.Consumer

/**
 * Default collection writer.
 */
class BinCollectionWriter : AbstractWrite() {

    /**
     * Serializes a `Collection<DatabaseRecord>` into a binary format.
     *
     * The serialized output contains metadata (collection ID, name, number of documents)
     * followed by each document’s size and serialized content. A `Consumer` is used to
     * optionally apply an operation to each document before serialization.
     *
     * The resulting format is:
     * - Long (8 bytes): Collection ID
     * - UTF String: Collection name
     * - Int (4 bytes): Number of documents
     * - Repeated per document:
     *   - Int (4 bytes): Size of serialized document
     *   - ByteArray: Serialized document bytes of the given size
     *
     * @param collection The collection to serialize.
     * @param consumer A `Consumer<Document>` that processes each document before serialization (e.g., for modification, transformation, logging).
     * @return A `ByteArray` containing the full serialized representation of the collection.
     */
    override fun writeCollection(collection: Collection<DatabaseRecord>, consumer: Consumer<Document>): ByteArray {
        val byteStream = ByteArrayOutputStream()
        val dataStream = DataOutputStream(byteStream)

        val documents = collection.collect()

        // Write collection details
        dataStream.writeLong(collection.id)
        dataStream.writeUTF(collection.name)

        dataStream.writeInt(documents.size)

        // Serialize each document
        for (doc in documents) {
            consumer.accept(doc)

            val docBytes = serializeDocument(doc)
            dataStream.writeInt(docBytes.size)
            dataStream.write(docBytes)
        }

        dataStream.flush()
        return byteStream.toByteArray()
    }

    /**
     * Serialize document to byte array.
     *
     * @param document the piece
     * @return the byte array
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun serializeDocument(document: Document): ByteArray {
        val byteStream = ByteArrayOutputStream()
        val dataStream = DataOutputStream(byteStream)

        dataStream.writeLong(document.id)
        dataStream.writeInt(document.flags)
        dataStream.writeUTF(document.serializeFlagsToString())
        dataStream.writeInt(document.index)

        dataStream.writeInt(document.size())

        for ((key, value) in document.elements) {
            if (value == null) continue

            dataStream.writeUTF(key)

            when (value) {
                is String -> {
                    dataStream.writeByte(1)
                    dataStream.writeUTF(value)
                }
                is Int -> {
                    dataStream.writeByte(2)
                    dataStream.writeInt(value)
                }
                is Long -> {
                    dataStream.writeByte(3)
                    dataStream.writeLong(value)
                }
                is Double -> {
                    dataStream.writeByte(4)
                    dataStream.writeDouble(value)
                }
                is Boolean -> {
                    dataStream.writeByte(5)
                    dataStream.writeBoolean(value)
                }
                is UUID -> {
                    dataStream.writeByte(6)
                    dataStream.writeUTF(value.toString())
                }
                is Serializable -> {
                    dataStream.writeByte(7)
                    val objectStream = ByteArrayOutputStream()
                    ObjectOutputStream(objectStream).use { it.writeObject(value) }
                    val objectBytes = objectStream.toByteArray()
                    dataStream.writeInt(objectBytes.size)
                    dataStream.write(objectBytes)
                }
                else -> throw IOException("Unsupported data type: ${value::class.simpleName}")
            }
        }

        dataStream.flush()
        return byteStream.toByteArray()
    }
}
