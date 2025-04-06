package net.cakemc.skrilla.database.serial.impl

import net.cakemc.database.api.Document
import java.io.*
import java.util.*

class Base64CollectionWriter {

    /**
     * Serialize a document to a Base64-encoded string.
     *
     * @param document the document to serialize
     * @return the Base64-encoded string
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun serializeDocumentToBase64(document: Document): String {
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
        return Base64.getEncoder().encodeToString(byteStream.toByteArray())
    }


}