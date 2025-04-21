package net.cakemc.skrilla.database.imdb

/**
 * Represents a batch of write operations to be applied to a database.
 * This class allows adding key-value pairs and removals to the batch.
 */
class WriteBatch {

    /**
     * A mutable list of key-value entries to be written.
     */
    var entries: MutableList<KeyValue> = ArrayList()

    /**
     * Adds a key-value pair to the batch.
     *
     * @param key The key to be added to the batch.
     * @param value The value associated with the key.
     */
    fun put(key: ByteArray, value: ByteArray) {
        entries.add(KeyValue(key, value))
    }

    /**
     * Adds a removal operation to the batch.
     *
     * @param key The key to be removed from the batch.
     */
    fun remove(key: ByteArray) {
        entries.add(KeyValue(key))
    }
}
