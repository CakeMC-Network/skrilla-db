package testing.network

import io.netty.channel.Channel
import net.cakemc.skrilla.networking.NetworkingClient
import net.cakemc.skrilla.networking.NetworkingServer
import net.cakemc.skrilla.networking.handler.ClientHandler
import net.cakemc.skrilla.networking.handler.PacketHandler
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketRegistry
import net.cakemc.skrilla.networking.packet.PacketType
import net.cakemc.skrilla.networking.packet.packets.auth.AuthRequestPacket
import net.cakemc.skrilla.networking.packet.packets.auth.AuthResponsePacket
import net.cakemc.skrilla.networking.packet.packets.auth.AuthStatus
import net.cakemc.skrilla.database.serial.SerializationSystem
import net.cakemc.skrilla.database.units.KeyManager
import net.cakemc.skrilla.event.DefaultEventBus
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher.SECRET_KEY
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

fun main() {
    SerializationSystem.registerEnumType(AuthStatus::class.java)
    SerializationSystem.registerEnumType(PacketIdentity::class.java)
    SerializationSystem.registerEnumType(PacketType::class.java)

    val registry = PacketRegistry()

    val clientHandler = ClientHandler(registry)

    clientHandler.registerPacketHandler(PacketIdentity.AUTH_RESPONSE, object : PacketHandler {
        override fun packetReceived(handler: ClientHandler, channel: Channel, packet: Packet) {
            // handle response
        }
    })

    val networkKey = KeyManager.getKey()
    val algorithm = "AES"

    val secretKey: SecretKey = SecretKeySpec(networkKey, algorithm)

    val client = NetworkingClient(secretKey, clientHandler, DefaultEventBus(10))

    Thread.ofVirtual().start({
        client.initialize()
        client.connect("0.0.0.0", 2233)
    })

    Thread.sleep(2000)
    clientHandler.registerChannel("main", client.activeChannel!!)

    val future = clientHandler.sendPacketWithFuture("main", AuthRequestPacket("admin", "password"))
    val value = future.syncUninterruptedly(2000, TimeUnit.MILLISECONDS)
    if (value is AuthResponsePacket) {
        println(AuthStatus.values()[value.authStatus])
    }
}