package recode

import io.netty.channel.Channel
import net.cakemc.skrilla.networking.NetworkingClient
import net.cakemc.skrilla.networking.NetworkingServer
import net.cakemc.skrilla.networking.handler.ClientHandler
import net.cakemc.skrilla.networking.handler.PacketHandler
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.packets.auth.AuthRequestPacket
import net.cakemc.skrilla.networking.packet.packets.auth.AuthResponsePacket
import net.cakemc.skrilla.networking.packet.packets.auth.AuthStatus
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

private
val ENCODED = byteArrayOf(-98, 110, 120, -120, -11, -37, -38, 92, 29, -79, 35, -52, 127, -31, -105, 17)
private
const val ALGORITHM = "AES"

val SECRET_KEY: SecretKey = SecretKeySpec(ENCODED, ALGORITHM)

fun main() {
    val serverClientHandler = ClientHandler()
    serverClientHandler.registerPacketHandler(PacketIdentity.AUTH_REQUEST, object : PacketHandler {
        override fun packetReceived(handler: ClientHandler, channel: Channel, packet: Packet) {
            handler.replyToPacketSync(channel, packet, AuthResponsePacket(AuthStatus.SUCCESS))
        }
    })

    val server = NetworkingServer(SECRET_KEY, serverClientHandler)

    Thread.ofVirtual().start({
        server.initialize()
        server.start("0.0.0.0", 2233)
    })

    val clientHandler = ClientHandler()

    val client = NetworkingClient(SECRET_KEY, clientHandler)

    Thread.ofVirtual().start({
        client.initialize()
        client.connect("0.0.0.0", 2233)
    })

    Thread.sleep(2000)
    clientHandler.registerChannel("main", client.activeChannel!!)

    val future = clientHandler.sendPacketWithFuture("main", AuthRequestPacket("test", "test"))
    val value = future.syncUninterruptedly(2000, TimeUnit.MILLISECONDS)
    if (value is AuthResponsePacket) {
        println(value.authStatus.name)
    }
}