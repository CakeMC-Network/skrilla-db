package net.cakemc.skrilla.natives.file

interface FileUtility {
    fun saveFile(filePath: String, data: ByteArray)
    fun readFile(filePath: String): ByteArray
}