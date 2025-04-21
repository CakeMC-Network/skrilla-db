package net.cakemc.database.api

import net.cakemc.skrilla.database.flags.DocumentFlags
import net.cakemc.skrilla.database.flags.FlagSerializer
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a document in the database that contains a collection of key-value pairs (elements),
 * along with flags that provide additional metadata or configuration for the document.
 *
 * A document is essentially a set of elements identified by a unique ID, and each element
 * can have different types such as Integer, Long, String, Boolean, etc. The document also has
 * a collection of flags that provide metadata or additional configuration that can be
 * serialized and deserialized.
 *
 * This class extends [DatabaseRecord] and implements [Serializable].
 *
 * @property index The index of the document in the database.
 * @property flags The flags associated with the document, represented as an integer.
 * @property id The unique identifier for the document.
 * @property elements A map of key-value pairs representing the elements stored in the document.
 * @property flagData A map storing the data for each flag.
 *
 * @constructor Creates a new [Document] with the provided index, flags, and ID.
 *
 * @see net.cakemc.skrilla.database.flags.DocumentFlags
 */
class Document(
    index: Int,
    flags: Int,
    id: Long,
    val elements: MutableMap<String, Any> = ConcurrentHashMap(),
    private val flagData: MutableMap<DocumentFlags, Any> = mutableMapOf()
) : DatabaseRecord(index, flags, id), Serializable {

    /**
     * Adds an integer element to the document.
     *
     * @param key The key associated with the element.
     * @param value The integer value to add.
     * @return The updated document.
     */
    fun add(key: String, value: Int): Document {
        elements[key] = value
        return this
    }

    /**
     * Adds a long element to the document.
     *
     * @param key The key associated with the element.
     * @param value The long value to add.
     * @return The updated document.
     */
    fun add(key: String, value: Long): Document {
        elements[key] = value
        return this
    }

    /**
     * Adds a double element to the document.
     *
     * @param key The key associated with the element.
     * @param value The double value to add.
     * @return The updated document.
     */
    fun add(key: String, value: Double): Document {
        elements[key] = value
        return this
    }

    /**
     * Adds a float element to the document.
     *
     * @param key The key associated with the element.
     * @param value The float value to add.
     * @return The updated document.
     */
    fun add(key: String, value: Float): Document {
        elements[key] = value
        return this
    }

    /**
     * Adds a boolean element to the document.
     *
     * @param key The key associated with the element.
     * @param value The boolean value to add.
     * @return The updated document.
     */
    fun add(key: String, value: Boolean): Document {
        elements[key] = value
        return this
    }

    /**
     * Adds a char element to the document.
     *
     * @param key The key associated with the element.
     * @param value The char value to add.
     * @return The updated document.
     */
    fun add(key: String, value: Char): Document {
        elements[key] = value
        return this
    }

    /**
     * Adds a byte element to the document.
     *
     * @param key The key associated with the element.
     * @param value The byte value to add.
     * @return The updated document.
     */
    fun add(key: String, value: Byte): Document {
        elements[key] = value
        return this
    }

    /**
     * Adds a short element to the document.
     *
     * @param key The key associated with the element.
     * @param value The short value to add.
     * @return The updated document.
     */
    fun add(key: String, value: Short): Document {
        elements[key] = value
        return this
    }

    /**
     * Adds a string element to the document.
     *
     * @param key The key associated with the element.
     * @param value The string value to add.
     * @return The updated document.
     */
    fun add(key: String, value: String): Document {
        elements[key] = value
        return this
    }

    /**
     * Sets the value for the specified key, overriding any existing value.
     *
     * @param key The key to set the value for.
     * @param value The value to associate with the key.
     * @return The updated document.
     */
    fun <T : Any> set(key: String, value: T): Document {
        elements[key] = value
        return this
    }

    /**
     * Retrieves the value associated with the given key and casts it to the specified type.
     *
     * @param key The key to retrieve the value for.
     * @param type The type to cast the value to.
     * @return The value cast to the specified type.
     * @throws ClassCastException if the value is not of the specified type.
     */
    fun <T> get(key: String, type: Class<T>): T {
        val value = elements[key]
        if (type.isInstance(value)) {
            return type.cast(value)
        }
        throw ClassCastException("Value for key '$key' is not of type ${type.name}")
    }

    // Methods for getting specific types (e.g., getInt, getLong, etc.) are also documented similarly:
    // - getInt(key: String)
    // - getLong(key: String)
    // - getDouble(key: String)
    // - getFloat(key: String)
    // - getBoolean(key: String)
    // - getChar(key: String)
    // - getByte(key: String)
    // - getShort(key: String)
    // - getString(key: String)

    /**
     * Checks if the document contains the specified key.
     *
     * @param key The key to check.
     * @return True if the key exists in the document, false otherwise.
     */
    fun contains(key: String): Boolean {
        return elements.containsKey(key)
    }

    /**
     * Checks if the document has the specified flag set.
     *
     * @param flag The flag to check.
     * @return True if the flag is set, false otherwise.
     */
    fun hasFlag(flag: DocumentFlags): Boolean = (flags and flag.bit) != 0

    /**
     * Adds the specified flag to the document.
     *
     * @param flag The flag to add.
     * @param data Optional data associated with the flag.
     */
    fun addFlag(flag: DocumentFlags, data: Any? = null) {
        flags = flags or flag.bit
        if (data != null) {
            flagData[flag] = data
        }
    }

    /**
     * Removes the specified flag from the document.
     *
     * @param flag The flag to remove.
     */
    fun removeFlag(flag: DocumentFlags) {
        flags = flags and flag.bit.inv()
    }

    /**
     * Toggles the specified flag (sets it if it's not set, unsets it if it's set).
     *
     * @param flag The flag to toggle.
     */
    fun toggleFlag(flag: DocumentFlags) {
        flags = flags xor flag.bit
    }

    /**
     * Retrieves the data associated with the specified flag.
     *
     * @param flag The flag to retrieve data for.
     * @return The data associated with the flag, or null if no data exists.
     */
    fun getFlagData(flag: DocumentFlags): Any? = flagData[flag]

    /**
     * Retrieves all flags currently set on the document.
     *
     * @return A set of all flags set on the document.
     */
    fun getAllFlags(): Set<DocumentFlags> = DocumentFlags.fromInt(flags)

    /**
     * Serializes the flags and their associated data to a string representation.
     *
     * @return A string representing the serialized flags and their data.
     */
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

    /**
     * Deserializes the flags and their associated data from a string representation.
     *
     * @param serialized The string representing the serialized flags and their data.
     */
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
     * Removes the element associated with the given key from the document.
     *
     * @param key The key of the element to remove.
     */
    fun remove(key: String) {
        elements.remove(key)
    }

    /**
     * Returns the size of the document (i.e., the number of elements).
     *
     * @return The number of elements in the document.
     */
    override fun size(): Int {
        return elements.size
    }

    /**
     * Returns a set of all keys in the document.
     *
     * @return A set of keys in the document.
     */
    fun keySet(): Set<String> {
        return elements.keys
    }

    /**
     * Returns a string representation of the document, including its index, ID, and elements.
     *
     * @return A string representing the document.
     */
    override fun toString(): String {
        return "Piece{" +
                "elements=" + elements +
                ", index=" + index +
                ", id=" + id +
                '}'
    }
}
