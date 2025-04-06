package net.cakemc.skrilla.natives.memory

import com.sun.jna.Library
import com.sun.jna.Pointer

interface MemoryFileLibrary : Library {
    fun create_memory_file(size: Long): Pointer
    fun release_memory_file(memory: Pointer, size: Long)
}