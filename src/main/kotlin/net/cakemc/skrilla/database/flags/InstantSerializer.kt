package net.cakemc.skrilla.database.flags

import java.time.Instant

/**
 * A [FlagSerializer] implementation for serializing and deserializing [Instant] objects.
 *
 * This serializer converts [Instant] objects into their string representations using
 * the [Instant.toString()] method and can deserialize those string representations
 * back into [Instant] objects using the [Instant.parse()] method.
 *
 * The [Instant] class in Java represents a specific moment on the timeline in UTC
 * with nanosecond precision. This serializer makes it easy to store and retrieve
 * [Instant] values in string format, making it compatible with storage systems
 * that deal with text-based data formats.
 */
object InstantSerializer : FlagSerializer<Instant> {

    /**
     * Serializes the given [Instant] into its string representation.
     *
     * @param data The [Instant] object to serialize.
     * @return The string representation of the [Instant].
     */
    override fun serialize(data: Instant): String = data.toString()

    /**
     * Deserializes the given string into an [Instant] object.
     *
     * @param serialized The string representation of the [Instant].
     * @return The deserialized [Instant] object.
     */
    override fun deserialize(serialized: String): Instant = Instant.parse(serialized)
}
