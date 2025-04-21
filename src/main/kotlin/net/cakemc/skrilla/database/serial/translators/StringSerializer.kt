package net.cakemc.skrilla.database.serial.translators

import net.cakemc.skrilla.database.serial.Serializer

class StringSerializer : Serializer<String> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as String)
    }

    override fun serialize(value: String): ByteArray {
        val stringBytes = value.toByteArray()
        val length = IntSerializer().serialize(stringBytes.size)
        return length + stringBytes
    }

    override fun deserialize(bytes: ByteArray): String {
        val length = IntSerializer().deserialize(bytes.copyOfRange(0, 4))
        return String(bytes.copyOfRange(4, 4 + length))
    }
}