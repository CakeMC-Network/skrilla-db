package net.cakemc.skrilla.database.natives.file.checksum

import com.sun.jna.Library

interface CheckSumLibrary : Library {
    fun calculate_file_sum(filename: String, checksum: ByteArray): Int
    fun validate_file(filename: String, expectedChecksum: String): Int
}