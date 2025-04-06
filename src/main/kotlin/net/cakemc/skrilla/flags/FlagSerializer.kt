package net.cakemc.skrilla.flags

interface FlagSerializer<T> {
    fun serialize(data: T): String
    fun deserialize(serialized: String): T
}
