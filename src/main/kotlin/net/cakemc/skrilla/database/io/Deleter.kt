package net.cakemc.skrilla.database.io

import net.cakemc.skrilla.database.imdb.Removable
import net.cakemc.skrilla.database.segment.Segment
import java.io.IOException
import java.lang.ref.Cleaner

/**
 * Interface representing a deletion mechanism for scheduling and removing files.
 * Implementations of this interface can schedule and perform file deletions.
 */
interface Deleter {

    /**
     * Schedules the deletion of the specified list of files.
     *
     * @param filesToDelete List of file names to be scheduled for deletion.
     * @throws IOException If an I/O error occurs while scheduling the deletion.
     */
    @Throws(IOException::class)
    fun scheduleDeletion(filesToDelete: List<String?>)

    /**
     * Deletes the files that have been scheduled for deletion.
     *
     * @throws IOException If an I/O error occurs while deleting the files.
     */
    @Throws(IOException::class)
    fun deleteScheduled()

    companion object {

        // The Cleaner instance that ensures automatic cleanup.
        private val cleaner: Cleaner = Cleaner.create()

        /**
         * Creates a Runnable action to remove a removable resource (like a file or segment)
         * using the provided `Removable` interface.
         *
         * This action will be triggered when the associated object is finalized.
         *
         * @param removable the object to be cleaned up (typically a Segment or similar).
         * @return a Runnable that invokes the `remove` method of the removable object.
         */
        private fun removeSegmentAction(removable: Removable): Runnable {
            return Runnable {
                try {
                    // The cleaner action removes the segment when it is finalized
                    removable.remove()
                } catch (e: Exception) {
                    // Optional: log this error or notify via a callback
                    // Removal might fail if the file is in use (e.g., during external backup)
                }
            }
        }

        /**
         * Registers a segment for removal when it is finalized.
         *
         * This method uses the Cleaner to ensure that the segment is removed when
         * it is no longer in use (i.e., when it is garbage collected).
         *
         * @param s the segment to be removed.
         * @param r the Removable resource associated with the segment.
         */
        fun removeOnFinalize(s: Segment, r: Removable) {
            cleaner.register(s, removeSegmentAction(r))
        }
    }
}
