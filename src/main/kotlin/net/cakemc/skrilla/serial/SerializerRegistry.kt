package net.cakemc.skrilla.serial

import kotlin.reflect.KClass

object SerializerRegistry {
    private val serializers = mutableMapOf<Class<*>, Serializer<*>>()

    fun <T : Any> registerSerializer(clazz: KClass<T>, serializer: Serializer<T>) {
        serializers[clazz.java] = serializer
    }

    fun <T : Any> registerSerializer(clazz: Class<T>, serializer: Serializer<T>) {
        serializers[clazz] = serializer
    }

    fun <T : Any> getSerializer(clazz: Class<T>): Serializer<T>? {
        return serializers[clazz] as? Serializer<T>
    }
}