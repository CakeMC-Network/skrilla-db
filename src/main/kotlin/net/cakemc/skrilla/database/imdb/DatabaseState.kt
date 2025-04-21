package net.cakemc.skrilla.database.imdb

import net.cakemc.skrilla.database.segment.MemorySegment
import net.cakemc.skrilla.database.segment.MultiSegment
import net.cakemc.skrilla.database.segment.Segment

/**
 * Represents the state of the database, including the list of segments and memory usage.
 * This class holds the database's segments and an optional memory segment and multi-segment object.
 *
 * @property segments The list of segments in the database.
 * @property memory The memory segment associated with the database, if any.
 * @property multi The multi-segment associated with the database, if any.
 */
class DatabaseState(
    val segments: List<Segment?>,
    val memory: MemorySegment?,
    multi: MultiSegment?
) {
    /**
     * The multi-segment associated with the database, if any.
     */
    val multi: Segment? = multi
}
