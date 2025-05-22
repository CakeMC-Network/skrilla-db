package net.cakemc.skrilla.datatrack

interface ReadWriteAdapter {

    fun put(key: String, value: Any?)
    fun get(key: String): Any?
    fun keys(): Set<String>

}