package net.cakemc.database.api

import net.cakemc.skrilla.flags.DocumentFlags
import net.cakemc.skrilla.flags.FlagSerializer
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * The type Piece.
 */
class Document
/**
 * Instantiates a new Piece.
 *
 * @param index    the index
 * @param id       the id
 * @param elements the elements
 */
    (index: Int, flags: Int, id: Long, val elements: MutableMap<String, Any> = ConcurrentHashMap(), private val flagData: MutableMap<DocumentFlags, Any> = mutableMapOf()) :

    DatabaseRecord(index, flags, id), Serializable {
    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: Int): Document {
        elements[key] = value
        return this
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: Long): Document {
        elements[key] = value
        return this
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: Double): Document {
        elements[key] = value
        return this
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: Float): Document {
        elements[key] = value
        return this
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: Boolean): Document {
        elements[key] = value
        return this
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: Char): Document {
        elements[key] = value
        return this
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: Byte): Document {
        elements[key] = value
        return this
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: Short): Document {
        elements[key] = value
        return this
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    fun add(key: String, value: String): Document {
        elements[key] = value
        return this
    }

    /**
     * Set.
     *
     * @param <T>   the type parameter
     * @param key   the key
     * @param value the value
    </T> */
    fun <T : Any> set(key: String, value: T): Document {
        elements[key] = value
        return this
    }

    /**
     * Get t.
     *
     * @param <T>  the type parameter
     * @param key  the key
     * @param type the type
     * @return the t
    </T> */
    fun <T> get(key: String, type: Class<T>): T {
        val value = elements[key]
        if (type.isInstance(value)) {
            return type.cast(value)
        }
        throw ClassCastException("Value for key '" + key + "' is not of type " + type.name)
    }


    /**
     * Gets int.
     *
     * @param key the key
     * @return the int
     */
    fun getInt(key: String): Int {
        val value = elements[key]
        if (value is Int) {
            return value
        }
        if (value is Double) {
            return value.toInt()
        }
        throw ClassCastException("Value for key '$key' is not of type int.")
    }

    /**
     * Gets long.
     *
     * @param key the key
     * @return the long
     */
    fun getLong(key: String): Long {
        val value = elements[key]
        if (value is Long) {
            return value
        }
        if (value is Double) {
            return value.toLong()
        }
        throw ClassCastException("Value for key '$key' is not of type long.")
    }

    /**
     * Gets double.
     *
     * @param key the key
     * @return the double
     */
    fun getDouble(key: String): Double {
        val value = elements[key]
        if (value is Double) {
            return value
        }
        throw ClassCastException("Value for key '$key' is not of type double.")
    }

    /**
     * Gets float.
     *
     * @param key the key
     * @return the float
     */
    fun getFloat(key: String): Float {
        val value = elements[key]
        if (value is Float) {
            return value
        }
        if (value is Double) {
            return value.toFloat()
        }
        throw ClassCastException("Value for key '$key' is not of type float.")
    }

    /**
     * Gets boolean.
     *
     * @param key the key
     * @return the boolean
     */
    fun getBoolean(key: String): Boolean {
        val value = elements[key]
        if (value is Boolean) {
            return value
        }
        throw ClassCastException("Value for key '$key' is not of type boolean.")
    }

    /**
     * Gets char.
     *
     * @param key the key
     * @return the char
     */
    fun getChar(key: String): Char {
        val value = elements[key]
        if (value is Char) {
            return value
        }
        throw ClassCastException("Value for key '$key' is not of type char.")
    }

    /**
     * Gets byte.
     *
     * @param key the key
     * @return the byte
     */
    fun getByte(key: String): Byte {
        val value = elements[key]
        if (value is Byte) {
            return value
        }
        if (value is Double) {
            return value.toInt().toByte()
        }
        throw ClassCastException("Value for key '$key' is not of type byte.")
    }

    /**
     * Gets short.
     *
     * @param key the key
     * @return the short
     */
    fun getShort(key: String): Short {
        val value = elements[key]
        if (value is Short) {
            return value
        }
        if (value is Double) {
            return value.toInt().toShort()
        }
        throw ClassCastException("Value for key '$key' is not of type short.")
    }

    /**
     * Gets string.
     *
     * @param key the key
     * @return the string
     */
    fun getString(key: String): String {
        val value = elements[key]
        if (value is String) {
            return value
        }
        throw ClassCastException("Value for key '$key' is not of type string.")
    }

    /**
     * Contains boolean.
     *
     * @param key the key
     * @return the boolean
     */
    fun contains(key: String): Boolean {
        return elements.containsKey(key)
    }

    fun hasFlag(flag: DocumentFlags): Boolean = (flags and flag.bit) != 0

    fun addFlag(flag: DocumentFlags, data: Any? = null) {
        flags = flags or flag.bit
        if (data != null) {
            flagData[flag] = data
        }
    }

    fun removeFlag(flag: DocumentFlags) {
        flags = flags and flag.bit.inv()
    }

    fun toggleFlag(flag: DocumentFlags) {
        flags = flags xor flag.bit
    }

    fun getFlagData(flag: DocumentFlags): Any? = flagData[flag]

    fun getAllFlags(): Set<DocumentFlags> = DocumentFlags.fromInt(flags)

    fun serializeFlagsToString(): String {
        return getAllFlags().joinToString(";") { flag ->
            val serializer = flag.serializer
            val value = flagData[flag]
            if (serializer != null && value != null) {
                @Suppress("UNCHECKED_CAST")
                "${flag.name}:${(serializer as FlagSerializer<Any>).serialize(value)}"
            } else {
                flag.name
            }
        }
    }

    fun deserializeFlagsFromString(serialized: String) {
        flags = 0
        flagData.clear()

        if (serialized.isBlank()) return

        serialized.split(";").forEach { entry ->
            val parts = entry.split(":", limit = 2)
            val flag = DocumentFlags.valueOf(parts[0])
            addFlag(flag)

            if (parts.size == 2) {
                val serializer = flag.serializer ?: return@forEach
                val value = @Suppress("UNCHECKED_CAST")
                (serializer as FlagSerializer<Any>).deserialize(parts[1])
                flagData[flag] = value
            }
        }
    }

    /**
     * Remove.
     *
     * @param key the key
     */
    fun remove(key: String) {
        elements.remove(key)
    }

    override fun size(): Int {
        return elements.size
    }

    fun keySet(): Set<String> {
        return elements.keys
    }

    override fun toString(): String {
        return "Piece{" +
                "elements=" + elements +
                ", index=" + index +
                ", id=" + id +
                '}'
    }
}
