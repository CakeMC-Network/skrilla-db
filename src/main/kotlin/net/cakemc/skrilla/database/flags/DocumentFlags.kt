package net.cakemc.skrilla.database.flags

/**
 * Enum class representing various flags that can be applied to a document. Each flag
 * is associated with a specific bit value, which allows the flags to be efficiently
 * stored as a single integer value. The class also provides functionality for converting
 * between integers and sets of flags.
 *
 * Flags:
 * - **AUTO_DELETE**: This flag indicates that the document should be automatically
 *   deleted. It is associated with the first bit (1 << 0) and uses a `FlagSerializer`
 *   for serialization.
 * - **FORCE_WRITE**: This flag forces the document to be written, associated with
 *   the second bit (1 << 1).
 *
 * @property bit The bit value representing the flag in a bitmask.
 * @property serializer The serializer for the flag, if any (optional).
 */
enum class DocumentFlags(val bit: Int, val serializer: FlagSerializer<*>? = null) {
    AUTO_DELETE(1 shl 0, InstantSerializer),
    FORCE_WRITE(1 shl 1),

    ;

    companion object {
        /**
         * Converts an integer flag value into a set of corresponding [DocumentFlags].
         *
         * @param flags The integer value representing the bitmask of flags.
         * @return A set of [DocumentFlags] corresponding to the bitmask.
         */
        fun fromInt(flags: Int): Set<DocumentFlags> =
            values().filter { flags and it.bit != 0 }.toSet()

        /**
         * Converts a vararg list of [DocumentFlags] into an integer bitmask.
         *
         * @param flags The flags to be converted into an integer.
         * @return An integer bitmask representing the provided flags.
         */
        fun toInt(vararg flags: DocumentFlags): Int =
            flags.fold(0) { acc, flag -> acc or flag.bit }

        /**
         * Converts a collection of [DocumentFlags] into an integer bitmask.
         *
         * @param flags A collection of flags to be converted into an integer.
         * @return An integer bitmask representing the provided flags.
         */
        fun toInt(flags: Collection<DocumentFlags>): Int =
            flags.fold(0) { acc, flag -> acc or flag.bit }
    }
}
