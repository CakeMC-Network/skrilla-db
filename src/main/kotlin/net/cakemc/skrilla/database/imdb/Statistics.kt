package net.cakemc.skrilla.database.imdb

/**
 * A data class representing the statistics of the database.
 * This class holds the number of segments and the number of block scans.
 *
 * @property numberOfSegments The total number of segments in the database.
 * @property blockScans The total number of block scans performed.
 */
data class Statistics(
    /**
     * The total number of segments in the database.
     */
    var numberOfSegments: Int = 0,

    /**
     * The total number of block scans performed.
     */
    var blockScans: Int = 0
)
