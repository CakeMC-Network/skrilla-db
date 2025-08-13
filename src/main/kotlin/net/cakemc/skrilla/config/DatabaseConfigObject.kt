package net.cakemc.skrilla.config

class DatabaseConfigObject(
    val hostName: String,
    val port: Int,

    val administrativeUser: String,
    val administrativePassword: String,

    val collectionMaxSize: Int,
    val documentMaxSize: Int,

    val maxCacheKeys: Int,
    val maxCacheValueSize: Int,
) {
}