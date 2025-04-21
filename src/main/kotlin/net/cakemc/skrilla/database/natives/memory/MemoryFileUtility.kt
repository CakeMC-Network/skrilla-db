package net.cakemc.skrilla.database.natives.memory

import com.sun.jna.Native
import com.sun.jna.Pointer
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

object MemoryFileUtility {

    private val library: net.cakemc.skrilla.database.natives.memory.MemoryFileLibrary = Native.loadLibrary(
        if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            "memory_file_util"
        } else {
            "lib_memory_file_util"
        },
        net.cakemc.skrilla.database.natives.memory.MemoryFileLibrary::class.java
    )


    /**
     * Creates a memory-backed temporary file.
     *
     * @param size The size of the memory file in bytes.
     * @return A pointer to the allocated memory.
     */
    fun createMemoryFile(size: Long): Pointer {
        return library.create_memory_file(size)
            ?: throw IllegalStateException("Failed to create memory file")
    }

    /**
     * Releases a memory-backed temporary file.
     *
     * @param memory The pointer to the memory region.
     * @param size The size of the memory file in bytes.
     */
    fun releaseMemoryFile(memory: Pointer, size: Long) {
        library.release_memory_file(memory, size)
    }


    fun writeMemoryToFile(memory: Pointer, size: Long, file: File) {
        val byteArray = ByteArray(size.toInt())

        memory.read(0, byteArray, 0, size.toInt())

        RandomAccessFile(file, "rw").use { raf ->
            val channel: FileChannel = raf.channel
            val buffer: ByteBuffer = ByteBuffer.wrap(byteArray)

            channel.write(buffer)
            channel.force(true)
        }

        releaseMemoryFile(memory, size)
    }

}