package net.cakemc.skrilla.database.imdb

import net.cakemc.skrilla.database.InMemoryDatabase
import net.cakemc.skrilla.database.io.Deleter
import net.cakemc.skrilla.database.io.SegmentStorage
import net.cakemc.skrilla.database.segment.MultiSegment
import net.cakemc.skrilla.database.segment.Segment
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport

/**
 * A utility object that handles background merging of database segments.
 * This object manages merging operations on disk segments for the database.
 */
internal object Merger {

    /**
     * The thread responsible for background merging of database segments.
     */
    var mergerThread: Thread? = null

    /**
     * Starts the background merging process.
     * Continuously attempts to merge database segments in the background until an error occurs or the database is closed.
     *
     * @param db The in-memory database to operate on.
     */
    fun backgroundMerge(db: InMemoryDatabase) {
        mergerThread = Thread.currentThread()

        while (true) {
            db.lock()
            if (!db.open || db.error != null) {
                db.unlock()
                return
            }

            // Release lock before merge
            db.unlock()

            try {
                mergeSegments0(db, Constants.maxSegments, true)
            } catch (e: Exception) {
                db.lock()
                db.error = e
                db.unlock()
            }

            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1))
        }
    }

    /**
     * Wakes up the background merger thread, if it's parked.
     */
    fun wakeupMerger() {
        LockSupport.unpark(mergerThread)
    }

    /**
     * Attempts to merge segments if the database is not already in the merge state.
     *
     * @param db The in-memory database to operate on.
     * @param segmentCount The maximum number of segments allowed before merging.
     * @param throttle Flag to enable throttling between merges.
     * @throws IOException If an error occurs during merging.
     */
    @Throws(IOException::class)
    fun mergeSegments0(db: InMemoryDatabase, segmentCount: Int, throttle: Boolean) {
        if (!db.inMerge.compareAndSet(false, true)) return
        try {
            mergeDiskSegments0Exclusive(db, segmentCount, throttle)
        } finally {
            db.inMerge.set(false)
        }
    }

    /**
     * Merges segments on disk while ensuring exclusive access to the merging process.
     *
     * @param db The in-memory database to operate on.
     * @param segmentCount The maximum number of segments allowed before merging.
     * @param throttle Flag to enable throttling between merges.
     * @throws IOException If an error occurs during merging.
     */
    @Throws(IOException::class)
    fun mergeDiskSegments0Exclusive(db: InMemoryDatabase, segmentCount: Int, throttle: Boolean) {
        while (true) {
            var segments = db.state!!.segments

            if (segments.size <= segmentCount) {
                return
            }

            val smallest = segments.indexOfMinBy { it?.size() ?: Long.MAX_VALUE }

            val index = if (smallest == segments.size - 1) smallest - 1 else smallest

            val maxMergeSize = maxOf(segments.size / 2, 4)

            val mergable = segments.subList(index, segments.size)
                .take(maxMergeSize)

            segments = segments.subList(index, index + mergable.size)

            val newSegment = mergeSegments1(db.deleter!!, db.path, segments, index == 0)
            db.lock()
            try {
                segments = db.state!!.segments
                for (i in mergable.indices) {
                    check(mergable[i] === segments[i + index]) { "unexpected segment change" }
                }
                mergable.forEach { it?.removeOnFinalize() }

                val newSegments = ArrayList<Segment?>().apply {
                    addAll(segments.subList(0, index))
                    add(newSegment)
                    addAll(segments.subList(index + mergable.size, segments.size))
                }

                db.state = DatabaseState(
                    newSegments,
                    db.state!!.memory,
                    MultiSegment(Segment.copyAndAppend(newSegments, db.state!!.memory))
                )
            } finally {
                db.unlock()
                if (throttle) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100))
                }
            }
        }
    }

    /**
     * Merges segments into a new segment and schedules the deletion of the old segments.
     *
     * @param deleter The deleter to schedule deletions.
     * @param dbpath The path to the database files.
     * @param segments The list of segments to merge.
     * @param removeDeleted Flag to determine whether to remove deleted data.
     * @return The newly merged segment.
     * @throws IOException If an error occurs during the merging process.
     */
    @Throws(IOException::class)
    fun mergeSegments1(deleter: Deleter, dbpath: String?, segments: List<Segment?>, removeDeleted: Boolean): Segment {
        val lowerId = segments.firstOrNull()?.lowerID()
        val upperId = segments.lastOrNull()?.upperID()

        val keyFilename = String.format("%s/keys.%d.%d", dbpath, lowerId, upperId)
        val dataFilename = String.format("%s/data.%d.%d", dbpath, lowerId, upperId)

        val files = segments.flatMap { it?.files() ?: emptyList() }

        val ms = MultiSegment(segments)
        val itr = ms.lookup(null, null)
        val seg: Segment = SegmentStorage.writeAndLoadSegment(keyFilename, dataFilename, itr, removeDeleted)

        deleter.scheduleDeletion(files)
        return seg
    }

    /**
     * Returns the index of the element with the minimum value according to the given selector.
     */
    private fun <T> List<T>.indexOfMinBy(selector: (T) -> Long): Int {
        return this.mapIndexed { index, item -> selector(item) to index }
            .minByOrNull { it.first }?.second ?: -1
    }
}
