package net.cakemc.skrilla.database.serial

/**
 * A generic serializer interface for converting objects to and from byte arrays.
 *
 * Implementations of this interface are responsible for defining how specific data types
 * are serialized and deserialized, which is essential for persistence or transmission.
 *
 * @param T The type of object this serializer handles.
 */
interface Serializer<T> {

    /**
     * Serializes the specified value of type [T] into a byte array.
     *
     * @param value The value to serialize.
     * @return A byte array representing the serialized value.
     */
    fun serialize(value: T): ByteArray

    /**
     * Serializes a value of any type into a byte array.
     *
     * This is useful when the exact type is not known at compile-time or for generic handling.
     * It's up to the implementation to ensure proper support for runtime type recognition.
     *
     * @param value The value to serialize.
     * @return A byte array representing the serialized object.
     */
    fun serializeAny(value: Any): ByteArray

    /**
     * Deserializes the given byte array back into an object of type [T].
     *
     * @param bytes The byte array to deserialize.
     * @return The deserialized object of type [T].
     */
    fun deserialize(bytes: ByteArray): T
}
