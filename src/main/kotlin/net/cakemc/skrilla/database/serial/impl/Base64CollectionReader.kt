package net.cakemc.skrilla.database.serial.impl

import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.collection.Collection
import net.cakemc.database.collection.DocumentCollection
import net.cakemc.database.serial.AbstractRead
import java.io.*
import java.util.*
import java.util.Base64.getDecoder

/**
 * Default collection reader from a Base64-encoded string.
 */
class Base64CollectionReader {

    /**
     * Reads a document from a Base64-encoded string.
     *
     * @param base64String the base64-encoded document
     * @return the deserialized document
     */
    fun readElement(base64String: String): Document {
        val data = getDecoder().decode(base64String)
        val byteStream = ByteArrayInputStream(data)
        val dataStream = DataInputStream(byteStream)

        val documentId = dataStream.readLong()
        val documentFlags = dataStream.readInt()
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

        return Document(documentIndex, documentFlags, documentId, values)
    }

}
