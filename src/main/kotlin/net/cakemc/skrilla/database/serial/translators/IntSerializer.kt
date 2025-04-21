package net.cakemc.skrilla.database.serial.translators

import net.cakemc.skrilla.database.serial.Serializer

class IntSerializer : Serializer<Int> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Int)
    }

    override fun serialize(value: Int): ByteArray {
        return ByteArray(4) { i -> (value shr (i * 8) and 0xFF).toByte() }
    }

    override fun deserialize(bytes: ByteArray): Int {
        return bytes.foldIndexed(0) { index, acc, byte ->
            acc or (byte.toInt() shl (index * 8))
        }
    }
}