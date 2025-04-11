package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer

class LongSerializer : Serializer<Long> {
    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Long)
    }

    override fun serialize(value: Long): ByteArray {
        val bytes = ByteArray(8)
        for (i in 7 downTo 0) {
            bytes[7 - i] = ((value ushr (i * 8)) and 0xFF).toByte()
        }
        return bytes
    }

    override fun deserialize(bytes: ByteArray): Long {
        require(bytes.size == 8) { "Byte array must be exactly 8 bytes long." }

        var result = 0L
        for (i in 0..7) {
            result = result or ((bytes[i].toLong() and 0xFF) shl ((7 - i) * 8))
        }
        return result
    }
}