package net.cakemc.skrilla.database.natives.memory

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.Pointer

// Interface to map the C standard library (libc)
interface MemoryLibrary : Library {
    // Memory operations
    fun malloc(size: Long): Pointer
    fun free(ptr: Pointer)
    fun memcpy(dest: Pointer, src: Pointer, n: Long): Pointer
    fun memset(dest: Pointer, c: Int, n: Long): Pointer

    companion object {
        val INSTANCE: MemoryLibrary = Native.loadLibrary(
            Platform.C_LIBRARY_NAME,
            MemoryLibrary::class.java
        )
    }
}