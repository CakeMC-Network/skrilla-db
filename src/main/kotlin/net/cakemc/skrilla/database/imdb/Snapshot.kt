package net.cakemc.skrilla.database.imdb

import net.cakemc.skrilla.database.InMemoryDatabase
import net.cakemc.skrilla.database.lookup.LookupIterator
import net.cakemc.skrilla.database.segment.MultiSegment
import java.io.IOException

/**
 * A read-only snapshot of the database at a specific point in time.
 * This class provides methods for accessing data and performing lookups.
 *
 * @property db The in-memory database associated with this snapshot.
 * @property multi The multi-segment instance used for data lookup.
 */
class Snapshot internal constructor(val db: InMemoryDatabase, val multi: MultiSegment) {

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key The key to look up in the snapshot.
     * @return The value associated with the key, or null if not found or empty.
     * @throws IOException If there is an error during the lookup.
     */
    @Throws(IOException::class)
    fun get(key: ByteArray): ByteArray? {
        // Get the value from multi and return null if it's empty or null
        return multi[key].takeIf { it?.isNotEmpty() == true }
    }

    /**
     * Creates a lookup iterator for a specified key range.
     *
     * @param lower The lower bound key of the range, or null if no lower bound.
     * @param higher The upper bound key of the range, or null if no upper bound.
     * @return A lookup iterator for the specified key range.
     * @throws IOException If there is an error during the lookup process.
     */
    @Throws(IOException::class)
    fun lookup(lower: ByteArray?, higher: ByteArray?): LookupIterator {
        // Return the iterator for the lookup range
        return db.newDatabaseLookup(multi.lookup(lower, higher))
    }
}
