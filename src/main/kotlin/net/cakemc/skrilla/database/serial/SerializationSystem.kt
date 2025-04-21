package net.cakemc.skrilla.database.serial

import net.cakemc.skrilla.database.serial.translators.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * A system for serializing and deserializing objects, including fields from the entire class hierarchy.
 *
 * This system registers default serializers for common types and provides functions for serializing
 * and deserializing objects with their fields from both the current class and any superclasses.
 * It handles serializing object fields and ensuring that objects can be deserialized properly
 * into instances of the correct class type, including the superclass fields.
 */
class SerializationSystem {

    /**
     * Initializes the [SerializationSystem] by registering default serializers
     * for common types such as primitive types, arrays, and standard types.
     */
    init {
        // Registering serializers for various common types.
        SerializerRegistry.registerSerializer(Int::class, IntSerializer())
        SerializerRegistry.registerSerializer(Boolean::class, BooleanSerializer())
        SerializerRegistry.registerSerializer(Float::class, FloatSerializer())
        SerializerRegistry.registerSerializer(Double::class, DoubleSerializer())
        SerializerRegistry.registerSerializer(Long::class, LongSerializer())
        SerializerRegistry.registerSerializer(IntArray::class, IntArraySerializer())
        SerializerRegistry.registerSerializer(BooleanArray::class, BooleanArraySerializer())
        SerializerRegistry.registerSerializer(FloatArray::class, FloatArraySerializer())
        SerializerRegistry.registerSerializer(LongArray::class, LongArraySerializer())
        SerializerRegistry.registerSerializer(String::class, StringSerializer())
        SerializerRegistry.registerSerializer(UUID::class, UUIDSerializer())
        SerializerRegistry.registerSerializer(Date::class, DateSerializer())
        SerializerRegistry.registerSerializer(Array<String>::class, StringArraySerializer())
        SerializerRegistry.registerSerializer(AtomicInteger::class, AtomicIntegerSerializer())
        SerializerRegistry.registerSerializer(AtomicLong::class, AtomicLongSerializer())
        SerializerRegistry.registerSerializer(AtomicBoolean::class, AtomicBooleanSerializer())
    }

    companion object {
        /**
         * Registers a serializer for an enum type.
         *
         * @param T The enum type to register.
         * @param clazz The class of the enum type.
         */
        fun <T: Enum<T>> registerEnumType(clazz: Class<T>) {
            SerializerRegistry.registerSerializer(clazz, EnumSerializer(clazz))
        }
    }

    /**
     * Serializes an object into a byte array, including all fields from the object's class hierarchy.
     * The function traverses the entire class hierarchy, serializing each field along the way.
     *
     * @param obj The object to serialize.
     * @return A byte array containing the serialized object data.
     */
    fun serialize(obj: Any): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()

        var currentClass: Class<*>? = obj.javaClass

        // Traverse through the entire class hierarchy
        while (currentClass != null) {
            // Serialize fields from the current class
            currentClass.declaredFields.forEach { field ->
                val serializer = SerializerRegistry.getSerializer(field.type)
                    ?: throw IllegalArgumentException("No serializer found for ${field.type}")

                field.isAccessible = true
                val fieldValue = field[obj]

                val dataSerialized = serializer.serializeAny(fieldValue)

                // Write field name length, field name, and serialized field data size
                byteArrayOutputStream.write(field.name.length)
                byteArrayOutputStream.write(field.name.toByteArray())

                byteArrayOutputStream.write(dataSerialized.size)
                byteArrayOutputStream.write(dataSerialized)
            }

            // Move up to the superclass
            currentClass = currentClass.superclass
        }

        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Deserializes a byte array into an object of the specified class type, including fields from the superclass.
     *
     * @param T The class type to deserialize into.
     * @param bytes The byte array containing the serialized data.
     * @param clazz The class type of the object to deserialize into.
     * @return The deserialized object, or `null` if deserialization fails.
     */
    fun <T : Any> deserialize(bytes: ByteArray, clazz: Class<T>): T? {
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        val nullObject = zeroInstanceJava(clazz)

        var currentClass: Class<*>? = clazz

        // Traverse through the entire class hierarchy
        while (currentClass != null) {
            while (byteArrayInputStream.available() > 0) {
                val fieldNameLength = byteArrayInputStream.read()
                val fieldNameData = ByteArray(fieldNameLength)
                byteArrayInputStream.read(fieldNameData)
                val fieldName = String(fieldNameData)

                val fieldValueLength = byteArrayInputStream.read()
                val fieldValueData = ByteArray(fieldValueLength)
                byteArrayInputStream.read(fieldValueData)

                if (currentClass.declaredFields.any { it.name.equals(fieldName) }) {
                    val field = currentClass.getDeclaredField(fieldName)
                    field.isAccessible = true

                    val serializer = SerializerRegistry.getSerializer(field.type)
                        ?: throw IllegalArgumentException("No serializer found for ${field.type}")

                    val fieldValue = serializer.deserialize(fieldValueData)

                    // Set the field value in the object (including superclass fields)
                    field.set(nullObject, fieldValue)
                } else {
                    val superClazz = currentClass.superclass
                    val field = superClazz.getDeclaredField(fieldName)
                    field.isAccessible = true

                    val serializer = SerializerRegistry.getSerializer(field.type)
                        ?: throw IllegalArgumentException("No serializer found for ${field.type}")

                    val fieldValue = serializer.deserialize(fieldValueData)

                    // Set the field value in the object (including superclass fields)
                    field.set(nullObject, fieldValue)
                }
            }

            // Move up to the superclass
            currentClass = currentClass.superclass
        }

        return nullObject
    }

    /**
     * Creates a zero-initialized instance of the specified class type.
     * This method uses reflection to determine the appropriate constructor and initializes fields
     * with default values for the given type.
     *
     * @param T The class type to instantiate.
     * @param clazz The class type to create an instance of.
     * @return A zero-initialized instance of the specified class, or `null` if instantiation fails.
     */
    fun <T : Any> zeroInstanceJava(clazz: Class<T>): T? {
        val constructors: Array<Constructor<*>> = clazz.constructors

        val constructor = constructors.minByOrNull { it.parameterCount }
            ?: return null

        val params = constructor.parameterTypes.map { paramType ->
            when (paramType) {
                String::class.java -> ""
                Int::class.java, java.lang.Integer::class.java -> 0
                Double::class.java, java.lang.Double::class.java -> 0.0
                Float::class.java, java.lang.Float::class.java -> 0f
                Long::class.java, java.lang.Long::class.java -> 0L
                Boolean::class.java, java.lang.Boolean::class.java -> false
                UUID::class.java -> UUID(0, 0)

                AtomicLong::class.java -> AtomicLong(0)
                AtomicInteger::class.java -> AtomicInteger(0)
                AtomicBoolean::class.java -> AtomicBoolean(false)

                Date::class.java -> Date(0)

                Array<String>::class.java -> arrayOf("")
                Array<Int>::class.java, IntArray::class.java -> arrayOf(0)
                Array<Double>::class.java, DoubleArray::class.java -> arrayOf(0.0)
                Array<Float>::class.java, FloatArray::class.java -> arrayOf(0F)
                Array<Boolean>::class.java, BooleanArray::class.java -> arrayOf(false)

                List::class.java -> emptyList<Any>()
                Map::class.java -> emptyMap<Any, Any>()

                else -> if (!Modifier.isAbstract(paramType.modifiers)) {
                    zeroInstanceJava(paramType)
                } else {
                    null
                }
            }
        }.toTypedArray()

        return try {
            constructor.newInstance(*params) as T
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
