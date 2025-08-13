package net.cakemc.skrilla

import io.netty.channel.Channel
import net.cakemc.database.AbstractDatabase
import net.cakemc.database.DefaultDatabase
import net.cakemc.skrilla.config.DatabaseConfigObject
import net.cakemc.skrilla.database.AbstractMemoryDatabase
import net.cakemc.skrilla.database.InMemoryDatabase
import net.cakemc.skrilla.database.imdb.Options
import net.cakemc.skrilla.database.serial.SerializationSystem
import net.cakemc.skrilla.database.units.JsonContainer
import net.cakemc.skrilla.database.units.KeyManager
import net.cakemc.skrilla.event.AbstractEventBus
import net.cakemc.skrilla.event.DefaultEventBus
import net.cakemc.skrilla.networking.NetworkingServer
import net.cakemc.skrilla.networking.handler.ClientHandler
import net.cakemc.skrilla.networking.handler.auth.DatabaseAuthHandler
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketRegistry
import net.cakemc.skrilla.networking.packet.PacketType
import net.cakemc.skrilla.networking.packet.packets.auth.AuthStatus
import java.nio.file.Path
import java.nio.file.Paths
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.io.path.exists


class Skrilla {

    val authedChannels: MutableList<Channel> = mutableListOf()

    val networkKey = KeyManager.getKey()
    val algorithm = "AES"

    val secretKey: SecretKey = SecretKeySpec(networkKey, algorithm)

    val registry: PacketRegistry
    val clientHandler: ClientHandler
    val networkingServer: NetworkingServer

    val configuration: DatabaseConfigObject

    val eventBus: AbstractEventBus

    init {
        SerializationSystem.registerEnumType(AuthStatus::class.java)
        SerializationSystem.registerEnumType(PacketIdentity::class.java)
        SerializationSystem.registerEnumType(PacketType::class.java)

        eventBus = DefaultEventBus(10)

        registry = PacketRegistry()
        clientHandler = ClientHandler(registry)

        networkingServer = NetworkingServer(
            secretKey, clientHandler, eventBus, registry
        )

        configuration = loadConfig()

        // todo load handlers here
        DatabaseAuthHandler(this)

        networkingServer.initialize()
    }

    fun loadConfig(): DatabaseConfigObject {
        var configObject: DatabaseConfigObject? = DatabaseConfigObject(
            "127.0.0.1", 2233, "admin", "password",
            20000, 1024 * 10, 10000, 512
        )

        val path = Paths.get("./database.json")
        if (path.exists()) {
            val container = JsonContainer.loadConfig(path)
            configObject = container.getObject("settings", DatabaseConfigObject::class.java)
            if (configObject == null)
                throw IllegalStateException("parsed config-object is invalid")

            return configObject
        } else {
            JsonContainer().append("settings", configObject).saveAsConfig(path)
            if (configObject == null)
                throw IllegalStateException("default config-object is null")
            return configObject
        }
    }

    fun start() {
        networkingServer.start(configuration.hostName, configuration.port)
    }

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