package net.cakemc.skrilla.database.io

import net.cakemc.skrilla.database.imdb.KeyComparison.Companion.newKeyCompare
import net.cakemc.skrilla.database.imdb.KeyValue
import net.cakemc.skrilla.database.imdb.Options
import net.cakemc.skrilla.database.imdb.Options.BatchReadMode
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

/**
 * LogFile handles writing log entries to a log file, supporting both normal writes and batch operations.
 * It allows for the efficient management of key-value pairs in a log, including synchronization and flush handling.
 *
 * @param path The directory path where the log file will be stored.
 * @param id The unique identifier for the log file.
 * @param options The options containing settings such as synchronization and flushing behavior.
 */
internal class LogFile(val path: Path, id: Long, options: Options) {
    var filepath: Path = path.resolve("log.$id")

    private val w: DataOutputStream
    private val id: Long = id
    private var inBatch = false

    private val syncWrite = options.enableSyncWrite
    private var disableFlush = options.disableWriteFlush && !syncWrite

    init {
        val fileOptions = mutableListOf(
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE
        ).apply {
            if (syncWrite) add(StandardOpenOption.SYNC)
        }

        w = DataOutputStream(
            UnSyncedBufferedOutputStream(
                Files.newOutputStream(filepath, *fileOptions.toTypedArray())
            )
        )
    }

    /**
     * Starts a batch of write operations with the given length.
     * This indicates that the following writes are part of a batch and should be written together.
     *
     * @param len The length of the batch.
     * @throws IOException If an error occurs while writing the batch header.
     */
    @Throws(IOException::class)
    fun startBatch(len: Int) {
        inBatch = true
        w.writeInt(-len)
    }

    /**
     * Ends a batch of write operations, flushing the batch to the log.
     *
     * @param len The length of the batch.
     * @throws IOException If an error occurs while finalizing the batch.
     */
    @Throws(IOException::class)
    fun endBatch(len: Int) {
        inBatch = false
        w.writeInt(-len)
        w.flush()
    }

    /**
     * Writes a key-value pair to the log file.
     *
     * @param key The key to write.
     * @param value The value to write.
     * @throws IOException If an error occurs while writing the key-value pair.
     */
    @Throws(IOException::class)
    fun write(key: ByteArray, value: ByteArray) {
        w.writeInt(key.size)
        w.write(key)
        w.writeInt(value.size)
        w.write(value)

        if (!inBatch && !disableFlush) {
            w.flush()
        }
    }

    /**
     * Closes the log file, ensuring all data is flushed.
     *
     * @throws IOException If an error occurs while closing the file.
     */
    @Throws(IOException::class)
    fun close() {
        w.flush()
        w.close()
    }

    /**
     * Removes the log file from the filesystem.
     *
     * @throws IOException If an error occurs while deleting the file.
     */
    @Throws(IOException::class)
    fun remove() {
        Files.delete(filepath)
    }

    /**
     * LogFileReader reads log entries from a log file.
     * It processes both individual key-value pairs and batches of entries.
     *
     * @param path The path to the log file to read from.
     * @param options The options for reading the log, including batch read behavior.
     */
    internal class LogFileReader private constructor(path: String, private val options: Options) {
        private val input: DataInputStream = DataInputStream(BufferedInputStream(FileInputStream(path)))
        private val list: ConcurrentSkipListMap<ByteArray, ByteArray> = ConcurrentSkipListMap(newKeyCompare(options))

        /**
         * Reads a single key-value entry from the log file.
         *
         * @param keyLen The length of the key in the entry.
         * @return The key-value pair read from the log.
         * @throws IOException If an error occurs while reading the entry.
         */
        @Throws(IOException::class)
        private fun readEntry(keyLen: Int): KeyValue {
            val key = ByteArray(keyLen)
            input.readFully(key)
            val valueLen = input.readInt()
            val value = ByteArray(valueLen)
            input.readFully(value)
            return KeyValue(key, value)
        }

        /**
         * Reads the entire log file, processing both individual entries and batches.
         *
         * @return A map of key-value pairs read from the log file.
         * @throws IOException If an error occurs while reading the log.
         */
        @Throws(IOException::class)
        private fun readLog(): ConcurrentSkipListMap<ByteArray, ByteArray> {
            input.use {
                while (true) {
                    val len = try {
                        input.readInt()
                    } catch (e: EOFException) {
                        break
                    }

                    if (len < 0) {
                        readBatch(len)
                    } else {
                        val kv = readEntry(len)
                        list[kv.key!!] = kv.value!!
                    }
                }
            }
            return list
        }

        /**
         * Reads a batch of key-value entries from the log file.
         *
         * @param len The length of the batch.
         * @throws IOException If an error occurs while reading the batch.
         */
        @Throws(IOException::class)
        private fun readBatch(len: Int) {
            val entries = mutableListOf<KeyValue>()
            try {
                repeat(-len) {
                    val keyLen = input.readInt()
                    entries.add(readEntry(keyLen))
                }
                val eob = input.readInt()
                if (eob != len) throw IOException("batch len does not match")
            } catch (e: IOException) {
                when (options.batchReadMode) {
                    BatchReadMode.RETURN_OPEN_ERROR -> throw e
                    BatchReadMode.DISCORD_PARTIAL -> return
                    else -> return
                }
            }
            for (kv in entries) {
                list[kv.key!!] = kv.value!!
            }
        }

        companion object {
            /**
             * Reads a log file and returns the key-value pairs contained within.
             *
             * @param path The path to the log file.
             * @param options The options for reading the log, including batch read behavior.
             * @return A map of key-value pairs read from the log file.
             * @throws IOException If an error occurs while reading the log file.
             */
            @Throws(IOException::class)
            fun readLogFile(path: String, options: Options): ConcurrentSkipListMap<ByteArray, ByteArray> {
                return LogFileReader(path, options).readLog()
            }
        }
    }

    companion object {
        /**
         * Reads a log file and returns the key-value pairs contained within.
         *
         * @param path The path to the log file.
         * @param options The options for reading the log, including batch read behavior.
         * @return A map of key-value pairs read from the log file.
         * @throws IOException If an error occurs while reading the log file.
         */
        @Throws(IOException::class)
        fun readLogFile(path: String, options: Options): ConcurrentSkipListMap<ByteArray, ByteArray> {
            return LogFileReader.readLogFile(path, options)
        }
    }
}
