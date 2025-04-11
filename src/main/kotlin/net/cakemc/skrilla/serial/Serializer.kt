package net.cakemc.skrilla.serial

interface Serializer<T> {
    fun serialize(value: T): ByteArray
    fun serializeAny(value: Any): ByteArray
    fun deserialize(bytes: ByteArray): T
}