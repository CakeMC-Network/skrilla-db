package net.cakemc.skrilla.networking.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.cakemc.skrilla.networking.handler.ClientHandler
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketType

class BossHandler(
    val clientHandler: ClientHandler
): SimpleChannelInboundHandler<Packet>() {

    override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet) {
        val responseId = packet.responseUUID

        if (packet.packetType.equals(PacketType.RESPONSE.ordinal)) {

            val pending = clientHandler.pendingPackets.get(responseId)
            if (pending != null)
                pending.set(packet)
        }

        clientHandler.packetReceived(ctx.channel(), packet)
    }

}