package net.cakemc.skrilla.database.natives.algorithm

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer

object DatabaseUtility {

    private val library: DatabaseLibrary = Native.loadLibrary(
        if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            "database_util"
        } else {
            "lib_database_util"
        },
        DatabaseLibrary::class.java
    )

    /**
     * Performs a binary search on a sorted array.
     *
     * @param sortedArray The sorted array of integers.
     * @param target The value to search for.
     * @return The index of the target, or -1 if not found.
     */
    fun binarySearch(sortedArray: IntArray, target: Int): Int {
        val size = sortedArray.size

        val pointer = Pointer.createConstant(size * Int.SIZE_BYTES.toLong())
        sortedArray.forEachIndexed { index, value ->
            pointer.setInt((index * Int.SIZE_BYTES).toLong(), value)
        }

        return library.binary_search(pointer, size, target)
    }

    fun intArrayToByteArray(array: IntArray): ByteArray {
        val byteArray = ByteArray(array.size * 4) // Each int is 4 bytes
        for (i in array.indices) {
            val intValue = array[i]
            byteArray[i * 4] = (intValue shr 24).toByte()
            byteArray[i * 4 + 1] = (intValue shr 16).toByte()
            byteArray[i * 4 + 2] = (intValue shr 8).toByte()
            byteArray[i * 4 + 3] = intValue.toByte()
        }
        return byteArray
    }

    /**
     * Updates the IntArray with values from the ByteArray.
     */
    fun updateArray(array: IntArray, pointer: ByteArray) {
        for (i in array.indices) {
            val intValue = (pointer[i * 4].toInt() shl 24) or
                    (pointer[i * 4 + 1].toInt() shl 16) or
                    (pointer[i * 4 + 2].toInt() shl 8) or
                    (pointer[i * 4 + 3].toInt())
            array[i] = intValue
        }
    }

    /**
     * Sorts an array in ascending order using forward_sort.
     */
    fun forwardSort(array: IntArray) {
        val size = array.size
        val byteArray = intArrayToByteArray(array)

        val memory = Memory(byteArray.size.toLong())
        memory.write(0, byteArray, 0, byteArray.size)

        library.forward_sort(memory, size)

        updateArray(array, byteArray)
    }

    /**
     * Sorts an array in descending order using backward_sort.
     */
    fun backwardSort(array: IntArray) {
        val size = array.size
        val byteArray = intArrayToByteArray(array)

        val memory = Memory(byteArray.size.toLong())
        memory.write(0, byteArray, 0, byteArray.size)

        library.backward_sort(memory, size)

        updateArray(array, byteArray)
    }

    /**
     * Helper to update the Kotlin array with values from native memory.
     */
    private fun updateArray(array: IntArray, pointer: Pointer) {
        for (i in array.indices) {
            array[i] = pointer.getInt((i * Int.SIZE_BYTES).toLong())
        }
    }

}