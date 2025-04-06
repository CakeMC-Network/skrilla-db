package net.cakemc.skrilla.database

class Index(var entries: MutableList<IndexEntry>) {
    fun getFileNameForDocument(id: Long): String? {
        return entries.firstOrNull { it.documentId == id }?.fileName
    }

    fun getOffsetForDocument(id: Long): Long? {
        return entries.firstOrNull { it.documentId == id }?.offset
    }
}