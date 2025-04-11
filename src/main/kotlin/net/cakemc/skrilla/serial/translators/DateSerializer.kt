package net.cakemc.skrilla.serial.translators

import net.cakemc.skrilla.serial.Serializer
import java.util.*

class DateSerializer : Serializer<Date> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Date)
    }

    override fun serialize(value: Date): ByteArray {
        return LongSerializer().serialize(value.time)
    }

    override fun deserialize(bytes: ByteArray): Date {
        val time = LongSerializer().deserialize(bytes)
        return Date(time)
    }
}