package net.cakemc.skrilla.flags

import java.time.Instant

object InstantSerializer : FlagSerializer<Instant> {
    override fun serialize(data: Instant): String = data.toString()
    override fun deserialize(serialized: String): Instant = Instant.parse(serialized)
}

object StringSerializer : FlagSerializer<String> {
    override fun serialize(data: String): String = data
    override fun deserialize(serialized: String): String = serialized
}
