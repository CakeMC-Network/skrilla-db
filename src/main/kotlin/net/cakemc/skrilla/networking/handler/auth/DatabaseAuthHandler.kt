package net.cakemc.skrilla.networking.handler.auth

import io.netty.channel.Channel
import net.cakemc.skrilla.Skrilla
import net.cakemc.skrilla.event.events.ChannelCloseEvent
import net.cakemc.skrilla.event.events.PacketIncomingEvent
import net.cakemc.skrilla.networking.handler.ClientHandler
import net.cakemc.skrilla.networking.handler.PacketHandler
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.packets.auth.AuthRequestPacket
import net.cakemc.skrilla.networking.packet.packets.auth.AuthResponsePacket
import net.cakemc.skrilla.networking.packet.packets.auth.AuthStatus

class DatabaseAuthHandler(val skrilla: Skrilla): PacketHandler {

    init {
        skrilla.clientHandler.registerPacketHandler(
            PacketIdentity.AUTH_REQUEST, this
        )

        skrilla.eventBus.register(ChannelCloseEvent::class.java) {
            skrilla.authedChannels.remove(it.channel)
        }
        skrilla.eventBus.register(PacketIncomingEvent::class.java) {
            if (it.packet is AuthRequestPacket)
                return@register

            it.cancelState = !skrilla.authedChannels.contains(it.channel)
        }
    }

    override fun packetReceived(handler: ClientHandler, channel: Channel, packet: Packet) {
        val authPacket = packet as AuthRequestPacket

        if (authPacket.user.isEmpty() && authPacket.password.isEmpty()) {
            handler.replyToPacketSync(channel, packet, AuthResponsePacket(AuthStatus.FAILED.ordinal))
            return
        }

        if (authPacket.user == skrilla.configuration.administrativeUser &&
            authPacket.password == skrilla.configuration.administrativePassword) {
            handler.replyToPacketSync(channel, packet, AuthResponsePacket(AuthStatus.SUCCESS.ordinal))

            skrilla.authedChannels.add(channel)
            return
        }
        handler.replyToPacketSync(channel, packet, AuthResponsePacket(AuthStatus.INVALID_CREDENTIALS.ordinal))
    }

}