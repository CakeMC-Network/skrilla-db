package recode

import net.cakemc.database.DefaultDatabase
import net.cakemc.database.filter.Filters
import net.cakemc.skrilla.flags.DocumentFlags
import java.nio.file.Paths
import java.time.Instant

// no =   8
// zstd = 2
// gzip = 2
// zlib = 2

// no   = 790 | 30ms
// zstd = 138 | 79ms
// gzip = 162 | 50ms
// zlib = 162 | 42ms


fun main() {
    val database = DefaultDatabase(Paths.get("./test/"))
    database.load()
    val collection = database.getCollection("players")

  //  for (i in 0 until 5000) {
  //      val document = collection.defineDocument()
  //      document.add("test$i", "owo")
  //      document.add("test$i", "uwu")
  //      collection.insertOneDocument(document)
  //  }
    collection.insertOneDocument(collection.defineDocument().add("test99", "uwu"))

    val start = System.currentTimeMillis()
    database.save()
    println(System.currentTimeMillis() - start)

    val document = collection.singleDocument(Filters.eq("test99", "uwu"))
    println(document.toString())

    if (document != null) {
        document.add("uwu", "uwu")
        collection.replaceOneDocument(Filters.eq("test99", "uwu"), document)
        println(document.getAllFlags())
        println(document.getFlagData(DocumentFlags.AUTO_DELETE))

        database.saveSingleDocument(document, "players")
    }


    println(collection.collect().size)
}
