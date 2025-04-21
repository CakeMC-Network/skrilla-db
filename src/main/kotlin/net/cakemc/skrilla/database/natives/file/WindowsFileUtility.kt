package net.cakemc.skrilla.database.natives.file

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.io.File

/**
 * Windows-specific implementation of FileUtility.
 */
class WindowsFileUtility : FileUtility {

    interface CLibrary : Library {
        fun fopen(filename: String, mode: String): Pointer?
        fun fread(buffer: ByteArray, size: Int, count: Int, stream: Pointer): Int
        fun fwrite(buffer: ByteArray, size: Int, count: Int, stream: Pointer): Int
        fun fclose(stream: Pointer): Int
    }

    private val cLibrary: CLibrary = Native.loadLibrary("msvcrt", CLibrary::class.java)

    override fun saveFile(filePath: String, data: ByteArray) {
        val filePointer = cLibrary.fopen(filePath, "wb")
            ?: throw Exception("Failed to open file: $filePath")

        try {
            val written = cLibrary.fwrite(data, 1, data.size, filePointer)
            if (written != data.size) {
                throw Exception("Failed to write all data to file: $filePath")
            }
        } finally {
            cLibrary.fclose(filePointer)
        }
    }

    override fun readFile(filePath: String): ByteArray {
        val filePointer = cLibrary.fopen(filePath, "rb")
            ?: throw Exception("Failed to open file: $filePath")

        return try {
            val file = File(filePath)
            val buffer = ByteArray(file.length().toInt())
            val read = cLibrary.fread(buffer, 1, buffer.size, filePointer)
            if (read != buffer.size) {
                throw Exception("Failed to read all data from file: $filePath")
            }
            buffer
        } finally {
            cLibrary.fclose(filePointer)
        }
    }
}
