package net.cakemc.skrilla.database.natives.file.checksum

import com.sun.jna.Native

object CheckSumUtility {
    private val library: net.cakemc.skrilla.database.natives.file.checksum.CheckSumLibrary = Native.loadLibrary(
        if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            "checksum"
        } else {
            "lib_checksum"
        },
        net.cakemc.skrilla.database.natives.file.checksum.CheckSumLibrary::class.java
    )

    /**
     * Calculates the SHA-256 checksum of a file.
     *
     * @param filename The file whose checksum is to be calculated.
     * @return The calculated checksum as a hexadecimal string.
     */
    fun calculateFileSum(filename: String): String {
        val checksum = ByteArray(64)
        val result = net.cakemc.skrilla.database.natives.file.checksum.CheckSumUtility.library.calculate_file_sum(filename, checksum)
        if (result != 0) {
            throw IllegalStateException("Failed to calculate file sum for: $filename")
        }

        return String(checksum)
    }

    /**
     * Validates a file by comparing its checksum with an expected checksum.
     *
     * @param filename The file to validate.
     * @param expectedChecksum The expected checksum.
     * @return True if the file matches the checksum, false otherwise.
     */
    fun validateFile(filename: String, expectedChecksum: String): Boolean {
        val result = net.cakemc.skrilla.database.natives.file.checksum.CheckSumUtility.library.validate_file(filename, expectedChecksum)
        return result == 1
    }
}