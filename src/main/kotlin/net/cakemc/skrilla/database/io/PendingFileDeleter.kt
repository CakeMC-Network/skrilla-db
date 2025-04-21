package net.cakemc.skrilla.database.io

import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.stream.Collectors

/**
 * The PendingFileDeleter class is responsible for scheduling and deleting files that need to be removed.
 * It writes file names to a "deleted" file and processes them later to actually delete the corresponding files.
 * This helps in deferring file deletion and ensuring that deletions are handled in a batch.
 *
 * @param path The directory path where the deleted files list will be stored and used for file deletions.
 */
class PendingFileDeleter(private val path: String) : Deleter {
    private var file: OutputStream? = null

    /**
     * Schedules a list of files for deletion by appending their names to a "deleted" file.
     *
     * The names of the files to be deleted are written in a comma-separated format followed by a newline.
     * The method ensures that the file where the names are stored is created if it does not exist,
     * and it writes the file names to the output stream in an efficient way.
     *
     * @param filesToDelete List of file names to be scheduled for deletion.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    @Synchronized
    @Throws(IOException::class)
    override fun scheduleDeletion(filesToDelete: List<String?>) {
        if (file == null) {
            val fileOptions = listOf(
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.SYNC
            )

            file = DataOutputStream(
                UnSyncedBufferedOutputStream(
                    Files.newOutputStream(
                        Path.of("$path/deleted"),
                        *fileOptions.toTypedArray()
                    )
                )
            )
        }

        val line = filesToDelete.joinToString(separator = ",", postfix = "\n")
        file!!.write(line.toByteArray())
    }

    /**
     * Deletes the files that were scheduled for deletion.
     *
     * This method first reads the names of the files from the "deleted" file, deletes the files if they exist,
     * and then removes the "deleted" file itself. It ensures that all deletions are handled properly
     * before cleaning up the tracking file.
     *
     * @throws IOException If an I/O error occurs while deleting the files or reading the "deleted" file.
     */
    @Synchronized
    @Throws(IOException::class)
    override fun deleteScheduled() {
        file?.close()
        file = null

        val deletedFilePath = Path.of("$path/deleted")
        if (!Files.exists(deletedFilePath)) return

        val files = Files.lines(deletedFilePath)
            .flatMap { line -> Arrays.stream(line.split(",").toTypedArray()) }
            .collect(Collectors.toList())

        for (fname in files) {
            Files.deleteIfExists(Path.of(path, fname))
        }

        Files.deleteIfExists(deletedFilePath)
    }
}
