package net.cakemc.skrilla.serial

import net.cakemc.skrilla.serial.translators.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class SerializationSystem {

    init {
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
        fun <T: Enum<T>> registerEnumType(clazz: Class<T>) {
            SerializerRegistry.registerSerializer(clazz, EnumSerializer(clazz))
        }
    }

    // Serialize function that includes fields from the superclass
    fun serialize(obj: Any): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()

        var currentClass: Class<*>? = obj.javaClass

        // Traverse through the entire class hierarchy
        while (currentClass != null) {
            // Include fields from the current class
            currentClass.declaredFields.forEach { field ->
                val serializer = SerializerRegistry.getSerializer(field.type)
                    ?: throw IllegalArgumentException("No serializer found for ${field.type}")

                field.isAccessible = true
                val fieldValue = field[obj]

                val dataSerialized = serializer.serializeAny(fieldValue)

                // Write field name length, name, and data size
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

    // Deserialize function that includes fields from the superclass
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

                    // Set the field in the object (even if it's in a superclass)
                    field.set(nullObject, fieldValue)
                } else {
                    val superClazz = currentClass.superclass
                    val field = superClazz.getDeclaredField(fieldName)
                    field.isAccessible = true

                    val serializer = SerializerRegistry.getSerializer(field.type)
                        ?: throw IllegalArgumentException("No serializer found for ${field.type}")

                    val fieldValue = serializer.deserialize(fieldValueData)

                    // Set the field in the object (even if it's in a superclass)
                    field.set(nullObject, fieldValue)
                }

            }

            // Move up to the superclass
            currentClass = currentClass.superclass
        }

        return nullObject
    }

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