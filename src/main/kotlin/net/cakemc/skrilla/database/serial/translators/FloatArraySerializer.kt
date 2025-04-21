package net.cakemc.skrilla.database.serial.translators

import net.cakemc.skrilla.database.serial.Serializer

class FloatArraySerializer : Serializer<FloatArray> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as FloatArray)
    }

    override fun serialize(value: FloatArray): ByteArray {
        val byteArray = ByteArray(value.size * 4)
        value.forEachIndexed { index, floatValue ->
            val bytes = FloatSerializer().serialize(floatValue)
            System.arraycopy(bytes, 0, byteArray, index * 4, 4)
        }
        return byteArray
    }

    override fun deserialize(bytes: ByteArray): FloatArray {
        val size = bytes.size / 4
        return FloatArray(size) { index ->
            FloatSerializer().deserialize(bytes.copyOfRange(index * 4, (index + 1) * 4))
        }
    }
}