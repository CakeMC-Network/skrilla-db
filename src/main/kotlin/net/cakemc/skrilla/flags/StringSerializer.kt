package net.cakemc.skrilla.flags

object StringSerializer : FlagSerializer<String> {
    override fun serialize(data: String): String = data
    override fun deserialize(serialized: String): String = serialized
}