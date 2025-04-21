package net.cakemc.skrilla.database.natives.algorithm

import com.sun.jna.Library
import com.sun.jna.Pointer

interface IdLibrary : Library {
    /**
     * Generates a random UUID and writes it to the provided buffer.
     * @param buffer Pointer to the buffer for storing the UUID string.
     */
    fun generate_uuid(buffer: Pointer)

    /**
     * Generates a random long integer.
     * @return A random long integer.
     */
    fun generate_random_long(): Long
}