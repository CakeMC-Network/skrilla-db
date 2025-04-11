package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer

class FloatSerializer : Serializer<Float> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Float)
    }

    override fun serialize(value: Float): ByteArray {
        return ByteArray(4) { i -> (value.toRawBits() shr (i * 8) and 0xFF).toByte() }
    }

    override fun deserialize(bytes: ByteArray): Float {
        val intBits = bytes.foldIndexed(0) { index, acc, byte ->
            acc or (byte.toInt() shl (index * 8))
        }
        return Float.fromBits(intBits)
    }
}