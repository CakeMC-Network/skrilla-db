package net.cakemc.skrilla.database.flags

/**
 * A [FlagSerializer] implementation for serializing and deserializing [String] objects.
 *
 * This serializer simply returns the string as is, without any transformation.
 * It is essentially a pass-through serializer that does not alter the [String] data
 * during serialization or deserialization. This can be useful when working with data
 * that is already in a string format, ensuring no conversion overhead.
 */
object StringSerializer : FlagSerializer<String> {

    /**
     * Serializes the given [String] by returning it as is.
     *
     * @param data The [String] to serialize.
     * @return The same [String] value.
     */
    override fun serialize(data: String): String = data

    /**
     * Deserializes the given string by returning it as is.
     *
     * @param serialized The [String] to deserialize.
     * @return The same [String] value.
     */
    override fun deserialize(serialized: String): String = serialized
}
