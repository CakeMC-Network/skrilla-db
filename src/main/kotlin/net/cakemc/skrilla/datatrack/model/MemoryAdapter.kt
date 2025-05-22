package net.cakemc.skrilla.datatrack.model

import net.cakemc.skrilla.datatrack.ReadWriteAdapter

class MemoryAdapter : ReadWriteAdapter {
    private val data = mutableMapOf<String, Any?>()

    override fun put(key: String, value: Any?) {
        data[key] = value
    }

    override fun get(key: String): Any? = data[key]

    override fun keys(): Set<String> = data.keys
}
