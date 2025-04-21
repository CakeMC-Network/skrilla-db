package net.cakemc.skrilla.database.serial.translators

import net.cakemc.skrilla.database.serial.Serializer
import java.util.concurrent.atomic.AtomicBoolean

class AtomicBooleanSerializer : Serializer<AtomicBoolean> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as AtomicBoolean)
    }

    override fun serialize(value: AtomicBoolean): ByteArray {
        return BooleanSerializer().serialize(value.get())
    }

    override fun deserialize(bytes: ByteArray): AtomicBoolean {
        val value = BooleanSerializer().deserialize(bytes)
        return AtomicBoolean(value)
    }
}