package net.cakemc.skrilla.networking.handler

import io.netty.channel.Channel
import net.cakemc.skrilla.networking.packet.Packet

interface PacketHandler {

    fun packetReceived(handler: ClientHandler, channel: Channel, packet: Packet)

}