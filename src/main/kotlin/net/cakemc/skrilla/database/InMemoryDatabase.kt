package net.cakemc.skrilla.database

import net.cakemc.skrilla.database.io.*
import net.cakemc.skrilla.database.lookup.LookupIterator
import net.cakemc.skrilla.database.segment.*
import net.cakemc.skrilla.database.imdb.*
import net.cakemc.skrilla.database.exceptions.*
import java.io.*
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.Volatile

/**
 * InMemoryDatabase represents an in-memory database with a persistent storage backend.
 * It supports basic database operations like PUT, GET, REMOVE, and snapshot creation.
 */
class InMemoryDatabase {

    // Lock to ensure thread-safety when accessing and modifying the database
    private val dbLock: ReentrantLock = ReentrantLock(false)

    // Atomic flags and counters for synchronization and concurrency control
    val inMerge: AtomicBoolean = AtomicBoolean(false)
    private val nextSegID: AtomicLong = AtomicLong()

    // WaitGroup for background operations
    private val waitGroup: WaitGroup = WaitGroup()

    // Flags and state tracking
    @Volatile
    var open: Boolean = false
    @Volatile
    var state: DatabaseState? = null
    var deleter: Deleter? = null
    var path: String? = null
    private var lockFile: LockFile? = null
    private var options: Options? = null
    var error: Exception? = null


    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key The key to look up
     * @return The associated value, or null if not found
     * @throws DatabaseClosedException if the database is closed
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun get(key: ByteArray): ByteArray? {
        if (!open) throw DatabaseClosedException()
        if (key.size == 0 || key.size > 1024) throw IOException("Invalid key length")
        val value = state!!.multi!![key]
        return if (value != null && value.size == 0) null else value
    }

    fun newDatabaseLookup(itr: LookupIterator): LookupIterator {
        return DatabaseLookup(itr, this)
    }

    /**
     * Inserts or updates a key-value pair in the database.
     *
     * @param key The key to insert or update
     * @param value The value to associate with the key
     * @throws DatabaseClosedException if the database is closed
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun put(key: ByteArray, value: ByteArray) {
        lock()
        try {
            if (!open) throw DatabaseClosedException()
            if (key.size == 0 || key.size > 1024) throw IOException("Invalid key length")

            maybeSwapMemory()
            state!!.memory!!.put(key, value)
        } finally {
            unlock()
            maybeMerge()
        }
    }

    /**
     * Removes the key-value pair associated with the specified key.
     *
     * @param key The key to remove
     * @return The removed value, or null if the key did not exist
     * @throws DatabaseClosedException if the database is closed
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun remove(key: ByteArray): ByteArray? {
        lock()
        try {
            if (!open) throw DatabaseClosedException()
            if (key.size == 0 || key.size > 1024) throw IOException("Invalid key length")
            val value = get(key) ?: return null
            maybeSwapMemory()
            state!!.memory!!.remove(key)
            return value
        } finally {
            unlock()
        }
    }

    /**
     * Executes a batch of write operations.
     *
     * @param batch The batch of operations to execute
     * @throws DatabaseClosedException if the database is closed
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun write(batch: WriteBatch) {
        lock()
        try {
            if (!open) {
                throw DatabaseClosedException()
            }
            maybeSwapMemory()
            state!!.memory!!.write(batch)
        } finally {
            unlock()
            maybeMerge()
        }
    }

    /**
     * Creates a read-only snapshot of the database at the current moment in time.
     *
     * @return A snapshot of the database
     * @throws DatabaseClosedException if the database is closed
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun snapshot(): Snapshot {
        lock()
        try {
            if (!open) {
                throw DatabaseClosedException()
            }
            val segments: List<Segment?> = if (state!!.memory!!.size() == 0L) {
                ArrayList(state!!.segments)
            } else {
                Segment.copyAndAppend(state!!.segments, state!!.memory)
            }
            return Snapshot(this, MultiSegment(segments))
        } finally {
            unlock()
        }
    }

    /**
     * Checks if the memory segment exceeds the maximum allowed size. If so, it moves memory to disk.
     */
    private fun maybeSwapMemory() {
        if (state!!.memory!!.size() > options!!.maxMemoryBytes) {
            val segments = Segment.copyAndAppend(state!!.segments, state!!.memory)
            val memory = MemorySegment(path, nextSegmentID(), options!!)
            val multi = MultiSegment(Segment.copyAndAppend(segments, memory))
            state = DatabaseState(segments, memory, multi)
        }
    }

    /**
     * Triggers a merge of the database segments if necessary.
     *
     * @throws IOException if an I/O error occurs during the merge operation
     */
    @Throws(IOException::class)
    private fun maybeMerge() {
        if (options!!.disableAutoMerge) return
        if (state!!.segments.size > 2 * options!!.maxSegments) {
            Merger.wakeupMerger()
        }
    }

    /**
     * Performs a range lookup within the database.
     *
     * @param lower The lower bound of the key range (inclusive)
     * @param upper The upper bound of the key range (inclusive)
     * @return An iterator over the key-value pairs in the range
     * @throws DatabaseClosedException if the database is closed
     */
    @Throws(IOException::class)
    fun lookup(lower: ByteArray?, upper: ByteArray?): LookupIterator? {
        if (!open) throw DatabaseClosedException()
        return snapshot().lookup(lower, upper)
    }

    /**
     * Locks the database for exclusive access.
     */
    fun lock() {
        dbLock.lock()
    }

    /**
     * Unlocks the database.
     */
    fun unlock() {
        dbLock.unlock()
    }

    /**
     * Returns the statistics for the current database.
     */
    fun stats(): Statistics {
        lock()
        try {
            val stats = Statistics()
            stats.numberOfSegments = state!!.segments.size
            return stats
        } finally {
            unlock()
        }
    }

    /**
     * Closes the database and triggers a merge if needed.
     *
     * @throws DatabaseException if closing the database fails
     * @throws IOException if there are I/O errors during the close operation
     */
    @Throws(DatabaseException::class, IOException::class)
    fun close() {
        closeWithMerge(options!!.maxSegments)
    }

    /**
     * Closes the database and performs a merge if the number of segments exceeds the specified threshold.
     *
     * @param numberOfSegments The maximum number of segments before merging
     * @throws DatabaseException if closing the database fails
     * @throws IOException if there are I/O errors during the close operation
     */
    @Throws(DatabaseException::class, IOException::class)
    fun closeWithMerge(numberOfSegments: Int) {
        synchronized(globalLock) {
            lock()
            try {
                if (!open) {
                    throw DatabaseException("Database is already closed.")
                }

                open = false
                unlock()

                waitGroup.waitEmpty()

                // Prepare state for closing
                state = DatabaseState(Segment.copyAndAppend(state!!.segments, state!!.memory), null, null)

                // Trigger merge if necessary
                if (numberOfSegments > 0) {
                    Merger.mergeSegments0(this, numberOfSegments, false)
                }

                // Write memory segments to disk asynchronously
                for (segment in state!!.segments) {
                    if (segment is MemorySegment) {
                        waitGroup.add(1)
                        executor.submit {
                            try {
                                SegmentStorage.writeSegmentToDisk(path, segment)
                            } catch (e: Exception) {
                                error = e
                            }
                            waitGroup.done()
                        }
                    }
                }

                waitGroup.waitEmpty()

                // Close all segments
                for (segment in state!!.segments) {
                    segment?.close()
                }

                deleter!!.deleteScheduled()

                if (error != null) {
                    throw DatabaseAsyncException(error)
                }
            } finally {
                state = DatabaseState(ArrayList(), null, null)
                unlock()
                lockFile!!.unlock()
            }
        }
    }

    /**
     * Returns the next available segment ID.
     */
    fun nextSegmentID(): Long = nextSegID.incrementAndGet()

    companion object {

        // Global lock for synchronizing access to database operations
        val globalLock: Any = Any()

        // Executor service for asynchronous background operations
        val executor: ExecutorService = Executors.newCachedThreadPool { runnable ->
            val thread = Thread(runnable, "db executorService")
            thread.isDaemon = true
            thread
        }

        // Default memory and segment configuration
        private const val dbMemorySegment = 1024 * 1024
        private const val dbMaxSegments = 8

        /**
         * Opens a database located at the specified path with the provided options.
         *
         * @param path The path to the database
         * @param options The options to configure the database
         * @return The opened in-memory database
         * @throws DatabaseException if an error occurs during opening
         */
        @JvmStatic
        @Throws(DatabaseException::class)
        fun open(path: String, options: Options): InMemoryDatabase {
            val copy = options.clone()
            synchronized(globalLock) {
                try {
                    return openImpl(path, copy)
                } catch (e: DatabaseNotFound) {
                    if (options.createIfNeeded) return create(path, copy)
                    throw e
                }
            }
        }

        /**
         * Creates a new in-memory database at the specified path with the given options.
         *
         * @param path The path to create the database
         * @param options The options to configure the database
         * @return The created in-memory database
         * @throws DatabaseException if an error occurs during creation
         */
        @Throws(DatabaseException::class)
        private fun create(path: String, options: Options): InMemoryDatabase {
            val dir = File(path)
            if (!dir.mkdirs()) throw DatabaseException("Unable to create directories.")
            return openImpl(path, options)
        }

        /**
         * Performs the actual implementation of opening the database.
         *
         * @param path The path to open
         * @param options The options to configure the database
         * @return The opened in-memory database
         * @throws DatabaseException if an error occurs during opening
         */
        @Throws(DatabaseException::class)
        private fun openImpl(path: String, options: Options): InMemoryDatabase {
            checkValidDatabase(path)

            var lockFile: LockFile? = null
            try {
                lockFile = LockFile("$path/lockfile")
            } catch (e: IOException) {
                throw DatabaseOpenFailed(e)
            }

            if (!lockFile.tryLock()) throw DatabaseInUseException()

            val database = InMemoryDatabase().apply {
                this.path = path
                this.lockFile = lockFile
                this.open = true
                this.options = options
                this.deleter = PendingFileDeleter(path)
            }

            // Delete any scheduled deletions
            try {
                database.deleter!!.deleteScheduled()
            } catch (e: IOException) {
                throw DatabaseCorruptedException(e)
            }

            var segments: List<Segment>? = null
            try {
                segments = DiskSegment.loadDiskSegments(path, options)
            } catch (e: IOException) {
                throw DatabaseCorruptedException(e)
            }

            val maxSegID = segments.maxOfOrNull { it.upperID() } ?: 0L
            database.nextSegID.set(maxSegID)

            val memory = MemorySegment(path, database.nextSegmentID(), options)
            val multi = MultiSegment(Segment.copyAndAppend(segments, memory))

            database.state = DatabaseState(segments, memory, multi)

            // Ensure memory and segment settings are adequate
            if (options.maxMemoryBytes < dbMemorySegment) {
                options.maxMemoryBytes = dbMemorySegment
            }
            if (options.maxSegments < dbMaxSegments) {
                options.maxSegments = dbMaxSegments
            }

            // If auto-merge is enabled, start the background merge process
            if (!options.disableAutoMerge) {
                database.waitGroup.add(1)
                executor.submit {
                    try {
                        Merger.backgroundMerge(database)
                    } finally {
                        database.waitGroup.done()
                    }
                }
            }

            return database
        }

        /**
         * Verifies if a database is valid at the specified path.
         *
         * @param path The path to check
         * @throws DatabaseNotFound if the database is not found
         * @throws DatabaseInvalid if the database is invalid
         */
        @Throws(DatabaseNotFound::class, DatabaseInvalid::class)
        private fun checkValidDatabase(path: String) {
            val file = File(path)
            if (!file.exists()) throw DatabaseNotFound()
            if (!file.isDirectory) throw DatabaseInvalid()

            file.listFiles()?.forEach { file ->
                when (file.name) {
                    "lockfile", "deleted" -> return@forEach
                    else -> if (!file.name.matches("(log|keys|data)\\..*".toRegex())) {
                        throw DatabaseInvalid()
                    }
                }
            }
        }

        /**
         * Removes the database at the specified path.
         *
         * @param path The path to remove
         * @throws DatabaseException if an error occurs during removal
         */
        @JvmStatic
        @Throws(DatabaseException::class)
        fun remove(path: String) {
            synchronized(globalLock) {
                checkValidDatabase(path)
                val lockFile = LockFile("$path/lockfile")
                if (!lockFile.tryLock()) throw DatabaseInUseException()

                purgeDirectory(File(path))
            }
        }

        /**
         * Recursively deletes all files and subdirectories within the given directory.
         *
         * @param dir The directory to purge
         */
        private fun purgeDirectory(dir: File) {
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isDirectory) purgeDirectory(file) // Recursive call for subdirectories
                    file.delete() // Attempt to delete the file
                }
            }
        }


    }

}
