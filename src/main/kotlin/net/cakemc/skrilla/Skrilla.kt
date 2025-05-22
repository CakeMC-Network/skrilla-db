package net.cakemc.skrilla

import net.cakemc.database.AbstractDatabase
import net.cakemc.database.DefaultDatabase
import net.cakemc.skrilla.database.AbstractMemoryDatabase
import net.cakemc.skrilla.database.InMemoryDatabase
import net.cakemc.skrilla.database.imdb.Options
import java.nio.file.Path
import java.nio.file.Paths

class Skrilla {

    companion object {

        fun defaultCache(name: String, root: Path = Paths.get("./")): AbstractMemoryDatabase {
            return InMemoryDatabase.create(root.resolve(name), Options.Builder().batchReadMode(Options.BatchReadMode.APPLY_PARTIAL).createIfNeeded(true).build())
        }
        fun configuredCache(name: String, options: Options, root: Path = Paths.get("./")): AbstractMemoryDatabase {
            return InMemoryDatabase.create(root.resolve(name), options)
        }

        fun defaultDatabase(name: String, root: Path = Paths.get("./")): AbstractDatabase {
            return DefaultDatabase(root.resolve(name))
        }

    }

}