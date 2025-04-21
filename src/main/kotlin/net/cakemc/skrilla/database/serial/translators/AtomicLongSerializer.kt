package net.cakemc.skrilla.database.serial.translators

import net.cakemc.skrilla.database.serial.Serializer
import java.util.concurrent.atomic.AtomicLong

class AtomicLongSerializer : Serializer<AtomicLong> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as AtomicLong)
    }

    override fun serialize(value: AtomicLong): ByteArray {
        return LongSerializer().serialize(value.get())
    }

    override fun deserialize(bytes: ByteArray): AtomicLong {
        val value = LongSerializer().deserialize(bytes)
        return AtomicLong(value)
    }
}