package net.cakemc.skrilla.database.serial.translators

import net.cakemc.skrilla.database.serial.Serializer

class BooleanArraySerializer : Serializer<BooleanArray> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as BooleanArray)
    }

    override fun serialize(value: BooleanArray): ByteArray {
        return ByteArray(value.size) { i -> if (value[i]) 1 else 0 }
    }

    override fun deserialize(bytes: ByteArray): BooleanArray {
        return BooleanArray(bytes.size) { i -> bytes[i] == 1.toByte() }
    }
}