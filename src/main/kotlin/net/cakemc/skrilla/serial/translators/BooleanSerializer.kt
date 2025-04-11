package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer

class BooleanSerializer : Serializer<Boolean> {
    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Boolean)
    }

    override fun serialize(value: Boolean): ByteArray {
        return byteArrayOf(if (value) 1 else 0)
    }

    override fun deserialize(bytes: ByteArray): Boolean {
        return bytes[0] == 1.toByte()
    }
}