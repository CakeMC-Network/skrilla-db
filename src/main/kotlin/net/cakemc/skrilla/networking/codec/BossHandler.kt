package net.cakemc.skrilla.networking.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.cakemc.skrilla.event.AbstractEventBus
import net.cakemc.skrilla.event.events.ChannelCloseEvent
import net.cakemc.skrilla.event.events.ChannelOpenEvent
import net.cakemc.skrilla.event.events.PacketIncomingEvent
import net.cakemc.skrilla.networking.handler.ClientHandler
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketType

class BossHandler(
    val clientHandler: ClientHandler,
    val eventBus: AbstractEventBus
): SimpleChannelInboundHandler<Packet>() {

    override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet) {
        val responseId = packet.responseUUID

        val event = PacketIncomingEvent(ctx.channel(), packet)
        eventBus.dispatch(event)

        if (event.cancelState) {
            return
        }

        if (packet.packetType.equals(PacketType.RESPONSE.ordinal)) {

            val pending = clientHandler.pendingPackets.get(responseId)
            if (pending != null)
                pending.set(packet)
        }

        clientHandler.packetReceived(ctx.channel(), packet)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)

        eventBus.dispatch(ChannelOpenEvent(ctx.channel()))
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)

        eventBus.dispatch(ChannelCloseEvent(ctx.channel()))
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        super.exceptionCaught(ctx, cause)
    }

}