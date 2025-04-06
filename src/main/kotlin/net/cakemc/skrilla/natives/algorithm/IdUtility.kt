package net.cakemc.skrilla.natives.algorithm

import com.sun.jna.Native
import com.sun.jna.Pointer

object IdUtility {
    const val BUFFER_SIZE = 37

    private val library: IdLibrary = Native.loadLibrary(
        if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            "random_id_util"
        } else {
            "lib_random_id_util"
        },
        IdLibrary::class.java
    )

    /**
     * Generates a random UUID as a string.
     * @return A randomly generated UUID string.
     */
    fun generateUUID(): String {
        val buffer = Pointer.createConstant(BUFFER_SIZE.toLong())
        library.generate_uuid(buffer)
        return buffer.getString(0)
    }

    /**
     * Generates a random long integer.
     * @return A randomly generated long integer.
     */
    fun generateRandomLong(): Long {
        return library.generate_random_long()
    }

}