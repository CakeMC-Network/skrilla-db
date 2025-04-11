package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer

class IntArraySerializer : Serializer<IntArray> {
    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as IntArray)
    }

    override fun serialize(value: IntArray): ByteArray {
        val byteArray = ByteArray(value.size * 4)
        value.forEachIndexed { index, intValue ->
            val bytes = IntSerializer().serialize(intValue)
            System.arraycopy(bytes, 0, byteArray, index * 4, 4)
        }
        return byteArray
    }

    override fun deserialize(bytes: ByteArray): IntArray {
        val size = bytes.size / 4
        return IntArray(size) { index ->
            IntSerializer().deserialize(bytes.copyOfRange(index * 4, (index + 1) * 4))
        }
    }
}