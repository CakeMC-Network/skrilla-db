package net.cakemc.skrilla.database.natives.compressor

import com.sun.jna.Library

interface CompressionLibrary : Library {
    fun compress_data(input: String, inputLen: Int, output: ByteArray, outputLen: IntArray): Int
    fun decompress_data(input: String, inputLen: Int, output: ByteArray, outputLen: IntArray): Int
}