package net.cakemc.skrilla.natives.memory

import com.sun.jna.Pointer

object MemoryUtility {

    fun allocateMemory(size: Int): Pointer {
        val pointerSize = size.toLong()
        return allocateMemory(pointerSize)
    }

    fun allocateMemory(size: Long): Pointer {
        val ptr = MemoryLibrary.INSTANCE.malloc(size)
        if (ptr == Pointer.NULL) {
            throw OutOfMemoryError("Failed to allocate memory of size $size bytes")
        }
        return ptr
    }

    fun freeMemory(pointer: Pointer) {
        if (pointer != Pointer.NULL) {
            MemoryLibrary.INSTANCE.free(pointer)
        }
    }

    fun copyMemory(dest: Pointer, src: Pointer, size: Long) {
        MemoryLibrary.INSTANCE.memcpy(dest, src, size)
    }

    fun setMemory(pointer: Pointer, value: Int, size: Long) {
        MemoryLibrary.INSTANCE.memset(pointer, value, size)
    }

    fun readMemory(pointer: Pointer, size: Int): ByteArray {
        val buffer = ByteArray(size)
        pointer.read(0, buffer, 0, size)
        return buffer
    }

    fun storeBytes(pointer: Pointer, data: ByteArray) {
        pointer.write(0, data, 0, data.size)
    }

    fun readBytes(pointer: Pointer, size: Int): ByteArray {
        val buffer = ByteArray(size)
        pointer.read(0, buffer, 0, size)
        return buffer
    }

}
