package net.cakemc.skrilla.database.units

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.SecureRandom

object KeyManager {

    private const val KEY_FILE_PATH = "./secret.key"
    private val KEY_SIZE = 16

    private val random = SecureRandom()

    fun getKey(): ByteArray {
        val keyFile = File(KEY_FILE_PATH)
        return try {
            if (!keyFile.exists()) {
                val key = ByteArray(KEY_SIZE)
                random.nextBytes(key)
                Files.write(Paths.get(KEY_FILE_PATH), key)
                key
            } else {
                Files.readAllBytes(Paths.get(KEY_FILE_PATH))
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to read or write key file", e)
        }
    }
}
