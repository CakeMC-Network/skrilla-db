package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer
import java.util.*

class UUIDSerializer : Serializer<UUID> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as UUID)
    }

    override fun serialize(value: UUID): ByteArray {
        val msbBytes = LongSerializer().serialize(value.mostSignificantBits)
        val lsbBytes = LongSerializer().serialize(value.leastSignificantBits)
        return msbBytes + lsbBytes
    }

    override fun deserialize(bytes: ByteArray): UUID {
        val msb = LongSerializer().deserialize(bytes.copyOfRange(0, 8))
        val lsb = LongSerializer().deserialize(bytes.copyOfRange(8, 16))
        return UUID(msb, lsb)
    }
}