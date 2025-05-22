package net.cakemc.database.serial.impl

import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.collection.Collection
import net.cakemc.database.collection.DocumentCollection
import net.cakemc.database.serial.AbstractRead
import java.io.*
import java.util.*

/**
 * Default collection reader.
 */
class BinCollectionReader : AbstractRead() {

    /**
     * Deserializes a binary-encoded collection from a byte array.
     *
     * This method reconstructs a `DocumentCollection` by reading its metadata (ID, name, document count),
     * followed by deserializing each individual document contained in the byte array.
     *
     * The expected binary format is:
     * - Long (8 bytes): Collection ID
     * - UTF String: Collection name
     * - Int (4 bytes): Number of documents
     * - Repeated per document:
     *   - Int (4 bytes): Size of the serialized document
     *   - ByteArray: Serialized document bytes of the given size
     *
     * @param data The byte array representing the serialized collection. Can be `null` but not recommended.
     * @return A `Collection<DatabaseRecord>` representing the fully reconstructed collection with all documents.
     */
    override fun read(data: ByteArray?): Collection<DatabaseRecord> {
        val byteStream = ByteArrayInputStream(data)
        val dataStream = DataInputStream(byteStream)

        // Read collection details
        val id = dataStream.readLong()
        val name = dataStream.readUTF()

        val numDocuments = dataStream.readInt()
        val documents: MutableList<DatabaseRecord> = ArrayList<DatabaseRecord>(numDocuments)

        // Deserialize each document
        for (i in 0 until numDocuments) {
            val docSize = dataStream.readInt()
            val docBytes = ByteArray(docSize)
            dataStream.readFully(docBytes)
            val doc = readElement(docBytes)
            documents.add(doc)
        }

        return DocumentCollection(documents, id, name)
    }

    /**
     * Read an element and return a Piece.
     *
     * @param data the data as a byte array
     * @return the deserialized Piece
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun readElement(data: ByteArray): Document {
        val byteStream = ByteArrayInputStream(data)
        val dataStream = DataInputStream(byteStream)

        val documentId = dataStream.readLong()
        val documentFlags = dataStream.readInt()
        val documentFlagsVals = dataStream.readUTF()
        val documentIndex = dataStream.readInt()

        val size = dataStream.readInt()
        val values = Hashtable<String, Any>(size)

        for (i in 0 until size) {
            val key = dataStream.readUTF()

            val typeId = dataStream.readByte()
            val value: Any? = when (typeId.toInt()) {
                1 -> dataStream.readUTF()
                2 -> dataStream.readInt()
                3 -> dataStream.readLong()
                4 -> dataStream.readDouble()
                5 -> dataStream.readBoolean()
                6 -> UUID.fromString(dataStream.readUTF())
                10 -> {
                    val objectSize = dataStream.readInt()
                    val objectBytes = ByteArray(objectSize)
                    dataStream.readFully(objectBytes)
                    readElement(objectBytes)
                }
                7 -> {
                    val objectSize = dataStream.readInt()
                    val objectBytes = ByteArray(objectSize)
                    dataStream.readFully(objectBytes)
                    val objectStream = ByteArrayInputStream(objectBytes)
                    ObjectInputStream(objectStream).use { it.readObject() }
                }
                else -> throw IOException("Unknown type identifier: $typeId")
            }
            values[key] = value
        }

        val document = Document(documentIndex, documentFlags, documentId, values)
        document.deserializeFlagsFromString(documentFlagsVals)
        return document
    }

    /**
     * Read an element and return a Piece.
     *
     * @param data the data as a byte array
     * @return the deserialized Piece
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun readElement(dataStream: DataInputStream): Document {
        val documentId = dataStream.readLong()
        val documentFlags = dataStream.readInt()
        val documentFlagsVals = dataStream.readUTF()
        val documentIndex = dataStream.readInt()

        val size = dataStream.readInt()
        val values = Hashtable<String, Any>(size)

        for (i in 0 until size) {
            val key = dataStream.readUTF()

            val typeId = dataStream.readByte()
            val value: Any? = when (typeId.toInt()) {
                1 -> dataStream.readUTF()
                2 -> dataStream.readInt()
                3 -> dataStream.readLong()
                4 -> dataStream.readDouble()
                5 -> dataStream.readBoolean()
                6 -> UUID.fromString(dataStream.readUTF())
                10 -> {
                    val objectSize = dataStream.readInt()
                    val objectBytes = ByteArray(objectSize)
                    dataStream.readFully(objectBytes)
                    readElement(objectBytes)
                }
                7 -> {
                    val objectSize = dataStream.readInt()
                    val objectBytes = ByteArray(objectSize)
                    dataStream.readFully(objectBytes)
                    val objectStream = ByteArrayInputStream(objectBytes)
                    ObjectInputStream(objectStream).use { it.readObject() }
                }
                else -> throw IOException("Unknown type identifier: $typeId")
            }
            values[key] = value
        }

        val document = Document(documentIndex, documentFlags, documentId, values)
        document.deserializeFlagsFromString(documentFlagsVals)
        return document
    }
}
