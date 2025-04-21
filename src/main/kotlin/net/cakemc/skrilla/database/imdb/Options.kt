package net.cakemc.skrilla.database.imdb

/**
 * Configuration options for the database.
 * This class contains various settings for database creation and behavior.
 *
 * @property createIfNeeded Flag indicating whether the database should be created if it doesn't exist.
 * @property disableAutoMerge Flag indicating whether automatic merging should be disabled.
 * @property maxSegments The maximum number of segments allowed.
 * @property maxMemoryBytes The maximum memory usage in bytes.
 * @property disableWriteFlush Flag indicating whether write flush should be disabled.
 * @property enableSyncWrite Flag indicating whether synchronous writes should be enabled.
 * @property batchReadMode The batch read mode used for read operations.
 * @property userKeyCompare An optional comparator for comparing user keys.
 */
class Options private constructor(
    val createIfNeeded: Boolean = false,
    val disableAutoMerge: Boolean = false,
    var maxSegments: Int = 0,
    var maxMemoryBytes: Int = 0,
    val disableWriteFlush: Boolean = false,
    val enableSyncWrite: Boolean = false,
    val batchReadMode: BatchReadMode = BatchReadMode.DISCORD_PARTIAL,
    val userKeyCompare: Comparator<ByteArray>? = null
) : Cloneable {

    /**
     * Empty constructor with default values for the options.
     */
    constructor() : this(
        createIfNeeded = false,
        disableAutoMerge = false,
        maxSegments = 0,
        maxMemoryBytes = 0,
        disableWriteFlush = false,
        enableSyncWrite = false,
        batchReadMode = BatchReadMode.DISCORD_PARTIAL,
        userKeyCompare = null
    )

    /**
     * Builder class for constructing an [Options] instance with custom configurations.
     */
    class Builder {
        var createIfNeeded: Boolean = false
        var disableAutoMerge: Boolean = false
        var maxSegments: Int = 0
        var maxMemoryBytes: Int = 0
        var disableWriteFlush: Boolean = false
        var enableSyncWrite: Boolean = false
        var batchReadMode: BatchReadMode = BatchReadMode.DISCORD_PARTIAL
        var userKeyCompare: Comparator<ByteArray>? = null

        /**
         * Sets the createIfNeeded flag.
         */
        fun createIfNeeded(createIfNeeded: Boolean) = apply { this.createIfNeeded = createIfNeeded }

        /**
         * Sets the disableAutoMerge flag.
         */
        fun disableAutoMerge(disableAutoMerge: Boolean) = apply { this.disableAutoMerge = disableAutoMerge }

        /**
         * Sets the maximum number of segments.
         */
        fun maxSegments(maxSegments: Int) = apply { this.maxSegments = maxSegments }

        /**
         * Sets the maximum memory usage in bytes.
         */
        fun maxMemoryBytes(maxMemoryBytes: Int) = apply { this.maxMemoryBytes = maxMemoryBytes }

        /**
         * Sets the disableWriteFlush flag.
         */
        fun disableWriteFlush(disableWriteFlush: Boolean) = apply { this.disableWriteFlush = disableWriteFlush }

        /**
         * Sets the enableSyncWrite flag.
         */
        fun enableSyncWrite(enableSyncWrite: Boolean) = apply { this.enableSyncWrite = enableSyncWrite }

        /**
         * Sets the batch read mode for the database.
         */
        fun batchReadMode(batchReadMode: BatchReadMode) = apply { this.batchReadMode = batchReadMode }

        /**
         * Sets the user key comparator.
         */
        fun userKeyCompare(userKeyCompare: Comparator<ByteArray>?) = apply { this.userKeyCompare = userKeyCompare }

        /**
         * Builds and returns an [Options] instance with the configured settings.
         */
        fun build(): Options {
            return Options(
                createIfNeeded,
                disableAutoMerge,
                maxSegments,
                maxMemoryBytes,
                disableWriteFlush,
                enableSyncWrite,
                batchReadMode,
                userKeyCompare
            )
        }
    }

    /**
     * Creates a copy of the current [Options] instance.
     */
    public override fun clone(): Options {
        return try {
            super.clone() as Options
        } catch (e: CloneNotSupportedException) {
            throw IllegalStateException("Options should support clone()")
        }
    }

    /**
     * Enum class representing the different batch read modes for reading data.
     */
    enum class BatchReadMode {
        /**
         * Discards partial results during a batch read operation.
         */
        DISCORD_PARTIAL,

        /**
         * Applies partial results during a batch read operation.
         */
        APPLY_PARTIAL,

        /**
         * Returns an error if an open operation encounters an issue.
         */
        RETURN_OPEN_ERROR
    }
}
