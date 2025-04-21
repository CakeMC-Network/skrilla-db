package net.cakemc.skrilla.database.serial

import kotlin.reflect.KClass

/**
 * A global registry for managing serializers.
 *
 * This registry allows serializers to be registered and retrieved by class type,
 * enabling a flexible and extensible way to handle object (de)serialization for various types.
 */
object SerializerRegistry {

    // Internal storage for class-to-serializer mappings.
    private val serializers = mutableMapOf<Class<*>, Serializer<*>>()

    /**
     * Registers a serializer for a given Kotlin class type.
     *
     * @param T The type this serializer handles.
     * @param clazz The Kotlin class reference of the type.
     * @param serializer The serializer implementation to register.
     */
    fun <T : Any> registerSerializer(clazz: KClass<T>, serializer: Serializer<T>) {
        serializers[clazz.java] = serializer
    }

    /**
     * Registers a serializer for a given Java class type.
     *
     * This overload allows registration using Java's [Class] object instead of Kotlin's [KClass].
     *
     * @param T The type this serializer handles.
     * @param clazz The Java class reference of the type.
     * @param serializer The serializer implementation to register.
     */
    fun <T : Any> registerSerializer(clazz: Class<T>, serializer: Serializer<T>) {
        serializers[clazz] = serializer
    }

    /**
     * Retrieves a serializer associated with the given Java class type.
     *
     * @param T The type for which a serializer is requested.
     * @param clazz The Java class reference of the type.
     * @return The registered [Serializer] for the type, or `null` if none is found.
     */
    fun <T : Any> getSerializer(clazz: Class<T>): Serializer<T>? {
        @Suppress("UNCHECKED_CAST")
        return serializers[clazz] as? Serializer<T>
    }
}
