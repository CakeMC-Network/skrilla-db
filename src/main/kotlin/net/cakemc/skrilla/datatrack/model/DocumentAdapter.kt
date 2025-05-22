package net.cakemc.skrilla.datatrack.model

import net.cakemc.database.api.Document
import net.cakemc.skrilla.datatrack.ReadWriteAdapter

class DocumentAdapter(
    val document: Document
): ReadWriteAdapter {

    override fun put(key: String, value: Any?) {
        when (value) {
            null -> {
            }
            is Boolean -> document.add(key, value)
            is Byte -> document.add(key, value)
            is Char -> document.add(key, value)
            is Double -> document.add(key, value)
            is Float -> document.add(key, value)
            is Int -> document.add(key, value)
            is Long -> document.add(key, value)
            is Short -> document.add(key, value)
            is String -> document.add(key, value)
            else -> throw IllegalArgumentException("Unsupported type for key '$key': ${value::class}")
        }
    }


    override fun get(key: String): Any? {
        return document.get(key, Any::class.java)
    }

    override fun keys(): Set<String> {
        return document.keySet()
    }

}