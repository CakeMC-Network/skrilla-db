package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer

class LongArraySerializer : Serializer<LongArray> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as LongArray)
    }

    override fun serialize(value: LongArray): ByteArray {
        val byteArray = ByteArray(value.size * 8)
        value.forEachIndexed { index, longValue ->
            val bytes = LongSerializer().serialize(longValue)
            System.arraycopy(bytes, 0, byteArray, index * 8, 8)
        }
        return byteArray
    }

    override fun deserialize(bytes: ByteArray): LongArray {
        val size = bytes.size / 8
        return LongArray(size) { index ->
            LongSerializer().deserialize(bytes.copyOfRange(index * 8, (index + 1) * 8))
        }
    }
}