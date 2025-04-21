package net.cakemc.skrilla.database.imdb

import java.util.*

/**
 * Interface for comparing keys in the database.
 * This interface provides a method for creating a comparator based on the provided options.
 */
interface KeyComparison {

    companion object {
        /**
         * Creates a comparator for byte arrays based on the provided options.
         * If a custom user-defined comparator is provided in the options, it will be used.
         * Otherwise, a default comparator based on `Arrays.compare()` is returned.
         *
         * @param options The options containing the user-defined comparator, if any.
         * @return A comparator for byte arrays.
         */
        fun newKeyCompare(options: Options): Comparator<ByteArray> {
            return options.userKeyCompare ?: Comparator { a, b -> Arrays.compare(a, b) }
        }
    }
}
