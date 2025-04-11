package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer
import java.util.concurrent.atomic.AtomicInteger

class AtomicIntegerSerializer : Serializer<AtomicInteger> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as AtomicInteger)
    }

    override fun serialize(value: AtomicInteger): ByteArray {
        return IntSerializer().serialize(value.get())
    }

    override fun deserialize(bytes: ByteArray): AtomicInteger {
        val value = IntSerializer().deserialize(bytes)
        return AtomicInteger(value)
    }
}