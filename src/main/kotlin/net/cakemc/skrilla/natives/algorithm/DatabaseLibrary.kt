package net.cakemc.skrilla.natives.algorithm

import com.sun.jna.Library
import com.sun.jna.Pointer

interface DatabaseLibrary : Library {

    /**
     * Native method for binary search.
     * @param array Pointer to the sorted array of integers.
     * @param size Size of the array.
     * @param target The value to search for.
     * @return Index of the target in the array, or -1 if not found.
     */
    fun binary_search(array: Pointer, size: Int, target: Int): Int


    /**
     * Native method for forward sorting.
     * @param array Pointer to the array of integers.
     * @param size Size of the array.
     */
    fun forward_sort(array: Pointer, size: Int)

    /**
     * Native method for backward sorting.
     * @param array Pointer to the array of integers.
     * @param size Size of the array.
     */
    fun backward_sort(array: Pointer, size: Int)
}