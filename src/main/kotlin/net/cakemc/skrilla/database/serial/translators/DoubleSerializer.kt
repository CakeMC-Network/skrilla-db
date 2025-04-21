package net.cakemc.skrilla.database.serial.translators

import net.cakemc.skrilla.database.serial.Serializer

class DoubleSerializer : Serializer<Double> {
    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Double)
    }

    override fun serialize(value: Double): ByteArray {
        return ByteArray(8) { i -> (value.toRawBits() shr (i * 8) and 0xFF).toByte() }
    }

    override fun deserialize(bytes: ByteArray): Double {
        val longBits = bytes.foldIndexed(0L) { index, acc, byte ->
            acc or (byte.toLong() shl (index * 8))
        }
        return Double.fromBits(longBits)
    }
}