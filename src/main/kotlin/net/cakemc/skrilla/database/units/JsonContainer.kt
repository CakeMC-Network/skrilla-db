package net.cakemc.skrilla.database.units

import com.google.gson.*
import com.google.gson.stream.JsonReader
import java.io.*
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * The type JsonContainer.
 */
@Suppress("UNUSED")
class JsonContainer : Serializable {

    private var cache: JsonObject

    /**
     * Instantiates a new JsonContainer.
     *
     * @param asJsonObject the as json object
     */
    internal constructor(asJsonObject: JsonObject) {
        this.cache = asJsonObject
    }

    /**
     * Instantiates a new JsonContainer.
     */
    constructor() {
        this.cache = JsonObject()
    }

    /**
     * Append configuration.
     *
     * @param key   the key
     * @param value the value
     * @return the configuration
     */
    fun append(key: String, value: String?): JsonContainer {
        cache.addProperty(key, value)
        return this
    }


    /**
     * Append configuration.
     *
     * @param key   the key
     * @param value the value
     * @return the configuration
     */
    fun append(key: String, value: Number?): JsonContainer {
        cache.addProperty(key, value)
        return this
    }

    /**
     * Append configuration.
     *
     * @param key   the key
     * @param value the value
     * @return the configuration
     */
    fun append(key: String, value: Boolean?): JsonContainer {
        cache.addProperty(key, value)
        return this
    }

    /**
     * Append configuration.
     *
     * @param key   the key
     * @param value the value
     * @return the configuration
     */
    fun append(key: String, value: JsonContainer): JsonContainer {
        cache.add(key, value.cache)
        return this
    }

    /**
     * Append configuration.
     *
     * @param key   the key
     * @param value the value
     * @return the configuration
     */
    fun append(key: String, value: Any?): JsonContainer {
        if (value == null) return this
        cache.add(key, GSON.toJsonTree(value))
        return this
    }

    /**
     * Append configuration.
     *
     * @param key   the key
     * @param value the value
     * @return the configuration
     */
    fun append(key: String, array: JsonArray?): JsonContainer {
        if (array == null) return this
        cache.add(key, array)
        return this
    }

    /**
     * Gets string.
     *
     * @param key the key
     * @return the string
     */
    fun getString(key: String?): String? {
        if (!cache.has(key)) return null
        return cache[key].asString
    }

    /**
     * Gets int.
     *
     * @param key the key
     * @return the int
     */
    fun getInt(key: String?): Int {
        if (!cache.has(key)) return 0
        return cache[key].asInt
    }

    /**
     * Gets long.
     *
     * @param key the key
     * @return the long
     */
    fun getLong(key: String?): Long {
        if (!cache.has(key)) return 0L
        return cache[key].asLong
    }

    /**
     * Gets double.
     *
     * @param key the key
     * @return the double
     */
    fun getDouble(key: String?): Double {
        if (!cache.has(key)) return 0.0
        return cache[key].asDouble
    }

    /**
     * Gets boolean.
     *
     * @param key the key
     * @return the boolean
     */
    fun getBoolean(key: String?): Boolean {
        if (!cache.has(key)) return false
        return cache[key].asBoolean
    }


    /**
     * Gets object.
     *
     * @param <T>    the type parameter
     * @param key    the key
     * @param class_ the class
     * @return the object
     */
    fun <T> getObject(key: String?, class_: Class<T>?): T? {
        if (!cache.has(key)) return null
        val element = cache[key]

        return GSON.fromJson(element, class_)
    }

    /**
     * Gets object.
     *
     * @param key  the key
     * @param type the type
     * @return the object
     */
    fun getObject(key: String?, type: Type?): Any? {
        if (!cache.has(key)) return null
        val element = cache[key]

        return GSON.fromJson(element, type)
    }

    /**
     * Gets array.
     *
     * @param key the key
     * @return the array
     */
    fun getArray(key: String?): JsonArray {
        return cache[key].asJsonArray
    }


    /**
     * Gets document.
     *
     * @param key the key
     * @return the document
     */
    fun getDocument(key: String?): JsonContainer {
        return JsonContainer(cache[key].asJsonObject)
    }

    /**
     * Contains key boolean.
     *
     * @param key the key
     * @return the boolean
     */
    fun containsKey(key: String?): Boolean {
        return cache.entrySet().stream().anyMatch { stringJsonElementEntry: Map.Entry<String, JsonElement?> ->
            stringJsonElementEntry.key.equals(
                key,
                ignoreCase = true
            )
        }
    }

    val keys: List<String>
        /**
         * Gets keys.
         *
         * @return the keys
         */
        get() {
            val keys: MutableList<String> = LinkedList()
            for ((key) in cache.entrySet()) keys.add(
                key
            )
            return keys
        }

    /**
     * Remove.
     *
     * @param entry the entry
     */
    fun remove(entry: String?): JsonContainer {
        cache.remove(entry)
        return this
    }

    /**
     * Convert to json string string.
     *
     * @return the string
     */
    fun convertToJsonString(): String {
        return Gson().toJson(this.cache)
    }

    //</editor-fold>
    /**
     * Save as config.
     *
     * @param backend the backend
     */
    fun saveAsConfig(backend: Path?) {
        if (backend == null) return

        try {
            // Delete existing file if it exists
            if (Files.exists(backend)) {
                Files.delete(backend)
            }

            // Open a buffered writer and write JSON
            Files.newBufferedWriter(
                backend,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            ).use { writer ->
                GSON.toJson(this.cache, writer)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(CloneNotSupportedException::class)
    fun clone(): JsonContainer {
        return JsonContainer(this.cache)
    }

    companion object {
        public val GSON: Gson = (GsonBuilder())
            .serializeNulls()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeSpecialFloatingPointValues()
            .enableComplexMapKeySerialization()
            .generateNonExecutableJson()
            .registerTypeAdapter(Long::class.java, LongAdapter())
            .create()

        class LongAdapter : JsonDeserializer<Long> {
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Long {
                return when {
                    json.isJsonNull -> 0L
                    json.asJsonPrimitive.isNumber -> json.asJsonPrimitive.asLong
                    json.asJsonPrimitive.isString -> json.asString.toLongOrNull() ?: 0L
                    else -> 0L
                }
            }
        }

        private const val serialVersionUID = -99841829603212185L

        /**
         * Load config configuration.
         *
         * @param backend the backend
         * @return the configuration
         */
        fun loadConfig(backend: Path): JsonContainer {
            return try {
                Files.newBufferedReader(backend, StandardCharsets.UTF_8).use { reader ->
                    JsonReader(reader).use { jsonReader ->
                        val jsonObject: JsonObject = GSON.fromJson(jsonReader, JsonObject::class.java)
                        JsonContainer(jsonObject)
                    }
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
                JsonContainer()
            }
        }

        /**
         * Load configuration.
         *
         * @param input the input
         * @return the configuration
         */
        fun load(input: String): JsonContainer {
            if (input.isEmpty()) {
                return JsonContainer()
            }
            return try {
                val jsonObject: JsonObject = GSON.fromJson(input, JsonObject::class.java)
                JsonContainer(jsonObject)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                JsonContainer()
            }
        }
    }
}