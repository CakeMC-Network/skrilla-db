package net.cakemc.skrilla.database.serial.translators

import net.cakemc.skrilla.database.serial.Serializer

class EnumSerializer<T : Enum<T>>(
    private val enumClass: Class<T>
) : Serializer<T> {

    override fun serialize(value: T): ByteArray {
        return value.name.toByteArray(Charsets.UTF_8)
    }

    override fun serializeAny(value: Any): ByteArray {
        if (value is Enum<*>) {
            return serialize(value as T)
        }
        throw IllegalArgumentException("Value is not an enum type")
    }

    override fun deserialize(bytes: ByteArray): T {
        val name = String(bytes, Charsets.UTF_8)
        return enumClass.enumConstants?.firstOrNull { it.name == name }
            ?: throw IllegalArgumentException("No enum constant found for name: $name")
    }
}
