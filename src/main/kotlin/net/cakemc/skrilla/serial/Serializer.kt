package net.cakemc.skrilla.serial

import java.io.*
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.*

interface Serializer<T> {
    fun serialize(value: T): ByteArray
    fun serializeAny(value: Any): ByteArray
    fun deserialize(bytes: ByteArray): T
}

class IntSerializer : Serializer<Int> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Int)
    }
    
    override fun serialize(value: Int): ByteArray {
        return ByteArray(4) { i -> (value shr (i * 8) and 0xFF).toByte() }
    }

    override fun deserialize(bytes: ByteArray): Int {
        return bytes.foldIndexed(0) { index, acc, byte ->
            acc or (byte.toInt() shl (index * 8))
        }
    }
}

class BooleanSerializer : Serializer<Boolean> {
    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Boolean)
    }

    override fun serialize(value: Boolean): ByteArray {
        return byteArrayOf(if (value) 1 else 0)
    }

    override fun deserialize(bytes: ByteArray): Boolean {
        return bytes[0] == 1.toByte()
    }
}

class FloatSerializer : Serializer<Float> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Float)
    }

    override fun serialize(value: Float): ByteArray {
        return ByteArray(4) { i -> (value.toRawBits() shr (i * 8) and 0xFF).toByte() }
    }

    override fun deserialize(bytes: ByteArray): Float {
        val intBits = bytes.foldIndexed(0) { index, acc, byte ->
            acc or (byte.toInt() shl (index * 8))
        }
        return Float.fromBits(intBits)
    }
}

class DoubleSerializer : Serializer<Double> {
    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Double)
    }

    override fun serialize(value: Double): ByteArray {
        return ByteArray(8) { i -> (value.toRawBits() shr (i * 8) and 0xFF).toByte() }
    }

    override fun deserialize(bytes: ByteArray): Double {
        val longBits = bytes.foldIndexed(0L) { index, acc, byte ->
            acc or (byte.toLong() shl (index * 8))
        }
        return Double.fromBits(longBits)
    }
}

class LongSerializer : Serializer<Long> {
    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Long)
    }

    override fun serialize(value: Long): ByteArray {
        val bytes = ByteArray(8)
        for (i in 7 downTo 0) {
            bytes[7 - i] = ((value ushr (i * 8)) and 0xFF).toByte()
        }
        return bytes
    }

    override fun deserialize(bytes: ByteArray): Long {
        require(bytes.size == 8) { "Byte array must be exactly 8 bytes long." }

        var result = 0L
        for (i in 0..7) {
            result = result or ((bytes[i].toLong() and 0xFF) shl ((7 - i) * 8))
        }
        return result
    }
}

class IntArraySerializer : Serializer<IntArray> {
    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as IntArray)
    }

    override fun serialize(value: IntArray): ByteArray {
        val byteArray = ByteArray(value.size * 4)
        value.forEachIndexed { index, intValue ->
            val bytes = IntSerializer().serialize(intValue)
            System.arraycopy(bytes, 0, byteArray, index * 4, 4)
        }
        return byteArray
    }

    override fun deserialize(bytes: ByteArray): IntArray {
        val size = bytes.size / 4
        return IntArray(size) { index ->
            IntSerializer().deserialize(bytes.copyOfRange(index * 4, (index + 1) * 4))
        }
    }
}

class BooleanArraySerializer : Serializer<BooleanArray> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as BooleanArray)
    }

    override fun serialize(value: BooleanArray): ByteArray {
        return ByteArray(value.size) { i -> if (value[i]) 1 else 0 }
    }

    override fun deserialize(bytes: ByteArray): BooleanArray {
        return BooleanArray(bytes.size) { i -> bytes[i] == 1.toByte() }
    }
}

class FloatArraySerializer : Serializer<FloatArray> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as FloatArray)
    }

    override fun serialize(value: FloatArray): ByteArray {
        val byteArray = ByteArray(value.size * 4)
        value.forEachIndexed { index, floatValue ->
            val bytes = FloatSerializer().serialize(floatValue)
            System.arraycopy(bytes, 0, byteArray, index * 4, 4)
        }
        return byteArray
    }

    override fun deserialize(bytes: ByteArray): FloatArray {
        val size = bytes.size / 4
        return FloatArray(size) { index ->
            FloatSerializer().deserialize(bytes.copyOfRange(index * 4, (index + 1) * 4))
        }
    }
}

class LongArraySerializer : Serializer<LongArray> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as LongArray)
    }

    override fun serialize(value: LongArray): ByteArray {
        val byteArray = ByteArray(value.size * 8)
        value.forEachIndexed { index, longValue ->
            val bytes = LongSerializer().serialize(longValue)
            System.arraycopy(bytes, 0, byteArray, index * 8, 8)
        }
        return byteArray
    }

    override fun deserialize(bytes: ByteArray): LongArray {
        val size = bytes.size / 8
        return LongArray(size) { index ->
            LongSerializer().deserialize(bytes.copyOfRange(index * 8, (index + 1) * 8))
        }
    }
}


class StringSerializer : Serializer<String> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as String)
    }

    override fun serialize(value: String): ByteArray {
        val stringBytes = value.toByteArray()
        val length = IntSerializer().serialize(stringBytes.size)
        return length + stringBytes
    }

    override fun deserialize(bytes: ByteArray): String {
        val length = IntSerializer().deserialize(bytes.copyOfRange(0, 4))
        return String(bytes.copyOfRange(4, 4 + length))
    }
}

class UUIDSerializer : Serializer<UUID> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as UUID)
    }

    override fun serialize(value: UUID): ByteArray {
        val msbBytes = LongSerializer().serialize(value.mostSignificantBits)
        val lsbBytes = LongSerializer().serialize(value.leastSignificantBits)
        return msbBytes + lsbBytes
    }

    override fun deserialize(bytes: ByteArray): UUID {
        val msb = LongSerializer().deserialize(bytes.copyOfRange(0, 8))
        val lsb = LongSerializer().deserialize(bytes.copyOfRange(8, 16))
        return UUID(msb, lsb)
    }
}

class DateSerializer : Serializer<Date> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Date)
    }

    override fun serialize(value: Date): ByteArray {
        return LongSerializer().serialize(value.time)
    }

    override fun deserialize(bytes: ByteArray): Date {
        val time = LongSerializer().deserialize(bytes)
        return Date(time)
    }
}

class StringArraySerializer : Serializer<Array<String>> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as Array<String>)
    }

    override fun serialize(value: Array<String>): ByteArray {
        val length = IntSerializer().serialize(value.size)
        val stringData = value.flatMap { StringSerializer().serialize(it).toList() }.toByteArray()
        return length + stringData
    }

    override fun deserialize(bytes: ByteArray): Array<String> {
        val length = IntSerializer().deserialize(bytes.copyOfRange(0, 4))
        var startIndex = 4
        return Array(length) {
            val strLength = IntSerializer().deserialize(bytes.copyOfRange(startIndex, startIndex + 4))
            startIndex += 4
            val strBytes = bytes.copyOfRange(startIndex, startIndex + strLength)
            startIndex += strLength
            String(strBytes)
        }
    }
}

class AtomicIntegerSerializer : Serializer<AtomicInteger> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as AtomicInteger)
    }

    override fun serialize(value: AtomicInteger): ByteArray {
        return IntSerializer().serialize(value.get())
    }

    override fun deserialize(bytes: ByteArray): AtomicInteger {
        val value = IntSerializer().deserialize(bytes)
        return AtomicInteger(value)
    }
}

class AtomicLongSerializer : Serializer<AtomicLong> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as AtomicLong)
    }

    override fun serialize(value: AtomicLong): ByteArray {
        return LongSerializer().serialize(value.get())
    }

    override fun deserialize(bytes: ByteArray): AtomicLong {
        val value = LongSerializer().deserialize(bytes)
        return AtomicLong(value)
    }
}

class AtomicBooleanSerializer : Serializer<AtomicBoolean> {

    override fun serializeAny(value: Any): ByteArray {
        return serialize(value as AtomicBoolean)
    }

    override fun serialize(value: AtomicBoolean): ByteArray {
        return BooleanSerializer().serialize(value.get())
    }

    override fun deserialize(bytes: ByteArray): AtomicBoolean {
        val value = BooleanSerializer().deserialize(bytes)
        return AtomicBoolean(value)
    }
}

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

    fun serialize(obj: Any): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()

        obj.javaClass.declaredFields.forEach { field ->
            val serializer = SerializerRegistry.getSerializer(field.type)
                ?: throw IllegalArgumentException("No serializer found for ${field.type}")

            field.isAccessible = true
            val fieldValue = field[obj]

            val dataSerialized = serializer.serializeAny(fieldValue)

            byteArrayOutputStream.write(field.name.length)
            byteArrayOutputStream.write(field.name.toByteArray())

            byteArrayOutputStream.write(dataSerialized.size)
            byteArrayOutputStream.write(dataSerialized)
        }

        return byteArrayOutputStream.toByteArray()
    }

    fun <T: Any> deserialize(bytes: ByteArray, clazz: Class<T>): T? {
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        val nullObject = zeroInstanceJava(clazz)

        while (byteArrayInputStream.available() > 0) {
            val fieldNameLength = byteArrayInputStream.read()
            val fieldNameData = ByteArray(fieldNameLength)
            byteArrayInputStream.read(fieldNameData)
            val fieldName = String(fieldNameData)

            val fieldValueLength = byteArrayInputStream.read()
            val fieldValueData = ByteArray(fieldValueLength)
            byteArrayInputStream.read(fieldValueData)

            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true

            val serializer = SerializerRegistry.getSerializer(field.type)
                ?: throw IllegalArgumentException("No serializer found for ${field.type}")

            val fieldValue = serializer.deserialize(fieldValueData)

            field.set(nullObject, fieldValue)
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

data class Person(var name: String, var age: Int, var isActive: Boolean,
                  var uuid: UUID, var birthDate: Date, var tags: Array<String>)

fun main() {
    val serializer = SerializationSystem()

    val person = Person("Alice", 30, true,
        UUID.randomUUID(), Date(), arrayOf("tag1", "tag2"))

    val serializedData = serializer.serialize(person)

    val deserializedPerson = serializer.deserialize(serializedData, Person::class.java)

    println("Original person: $person")
    println("Deserialized person: $deserializedPerson")
}
