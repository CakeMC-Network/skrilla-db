package net.cakemc.skrilla.networking.codec.packet

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.cakemc.skrilla.networking.packet.PacketRegistry

class PacketDecoder(
    val registry: PacketRegistry
): ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, output: MutableList<Any>) {
        val packetId = input.readInt()
        val packet = registry.createPacketOutOfId(packetId)
        if (packet != null) {
            packet.read(input)

            output.add(packet)
        }
    }

}