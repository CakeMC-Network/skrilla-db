package net.cakemc.skrilla.database.flags

/**
 * Interface for serializing and deserializing flag data. Implementing this interface
 * allows custom serialization logic for flags, which are typically represented as
 * a specific data type (e.g., `Instant`, `String`, etc.).
 *
 * The main purpose of this interface is to allow flags to be serialized into a
 * storable format (e.g., string) and later deserialized back into the original
 * data type.
 *
 * @param T The type of the data that the serializer will handle.
 */
interface FlagSerializer<T> {

    /**
     * Serializes the given data into a string format.
     *
     * @param data The data to be serialized.
     * @return A string representation of the serialized data.
     */
    fun serialize(data: T): String

    /**
     * Deserializes the given string into the original data type.
     *
     * @param serialized The string representation of the serialized data.
     * @return The deserialized data of type [T].
     */
    fun deserialize(serialized: String): T
}
