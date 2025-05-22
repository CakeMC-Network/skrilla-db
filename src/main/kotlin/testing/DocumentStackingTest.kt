package testing

import net.cakemc.database.DefaultDatabase
import net.cakemc.database.filter.Filters
import java.nio.file.Paths

fun main() {
    create()
    load()
}

fun load() {
    val database = DefaultDatabase(Paths.get("./test/"))
    database.load()
    val collection = database.getCollection("players")

    val document = collection.findDocument(Filters.eq("uid", "test"))
    val inner = document!!.getDocument("test")

    val value = inner.getString("test")

    println(value)
    println(inner.elements)
}

fun create() {
    val database = DefaultDatabase(Paths.get("./test/"))
    database.load()
    val collection = database.getCollection("players")

    val document = collection.defineDocument()
    document.add("uid", "test")
    document.add("test", collection.defineDocument().add("test", "test"))

    collection.insertOneDocument(document)
    database.save()
}