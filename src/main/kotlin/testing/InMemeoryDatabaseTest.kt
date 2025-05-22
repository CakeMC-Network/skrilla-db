package testing

import net.cakemc.skrilla.database.InMemoryDatabase
import net.cakemc.skrilla.database.imdb.Options
import java.nio.file.Paths

fun main() {
    val memoryDatabase = InMemoryDatabase.create(Paths.get("./test"), Options.Builder().createIfNeeded(true).batchReadMode(Options.BatchReadMode.APPLY_PARTIAL).build())
    //memoryDatabase.put("test".toByteArray(), "test".toByteArray())
    println(String(memoryDatabase.get("test".toByteArray())!!))
}