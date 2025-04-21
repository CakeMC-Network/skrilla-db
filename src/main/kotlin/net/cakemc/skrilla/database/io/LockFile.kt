package net.cakemc.skrilla.database.io

import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * LockFile provides file locking functionality for managing exclusive access to a file.
 * It allows attempting to lock a file and unlocking it when the operation is done.
 *
 * @param path The path of the file to lock.
 */
class LockFile(path: String) {
    private val path: Path = Path.of(path)
    var fch: FileChannel? = null
        private set
    private var lock: FileLock? = null

    /**
     * Attempts to acquire an exclusive lock on the file.
     * If the lock is acquired successfully, returns true; otherwise, returns false.
     *
     * @return true if the lock was successfully acquired, false otherwise.
     */
    fun tryLock(): Boolean {
        return try {
            // Open the file for writing and create it if it doesn't exist
            fch = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
            // Try to acquire an exclusive lock on the file
            lock = fch?.tryLock()
            // Return true if the lock was successfully acquired, false otherwise
            lock != null
        } catch (e: IOException) {
            // Return false if any IOException occurs
            false
        }
    }

    /**
     * Releases the lock and closes the file channel.
     * This method ensures that the file is unlocked and the resources are released.
     */
    fun unlock() {
        try {
            // Release the lock and close the file channel
            lock?.release()
            fch?.close()
        } catch (_: IOException) {
            // Ignore any IOException during the release or close operations
        } finally {
            // Reset lock and file channel to null
            lock = null
            fch = null
        }
    }
}
