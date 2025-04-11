package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer

class StringArraySerializer : Serializer<Array<String>> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Array<String>)
    }

    override fun serialize(value: Array<String>): ByteArray {
        val length = IntSerializer().serialize(value.size)
        val stringData = value.flatMap { StringSerializer().serialize(it).toList() }.toByteArray()
        return length + stringData
    }

    override fun deserialize(bytes: ByteArray): Array<String> {
        val length = IntSerializer().deserialize(bytes.copyOfRange(0, 4))
        var startIndex = 4
        return Array(length) {
            val strLength = IntSerializer().deserialize(bytes.copyOfRange(startIndex, startIndex + 4))
            startIndex += 4
            val strBytes = bytes.copyOfRange(startIndex, startIndex + strLength)
            startIndex += strLength
            String(strBytes)
        }
    }
}