package net.cakemc.skrilla.database.imdb

/**
 * Represents a key-value pair in the database.
 *
 * @property key The key associated with the value.
 * @property value The value associated with the key.
 */
class KeyValue(val key: ByteArray, val value: ByteArray) {

    /**
     * Secondary constructor for creating a KeyValue with only a key.
     * The value is set to an empty byte array.
     *
     * @param key The key associated with the value.
     */
    constructor(key: ByteArray) : this(key, EMPTY)

    companion object {
        /**
         * An empty byte array to represent the absence of a value.
         */
        val EMPTY = ByteArray(0)
    }
}
