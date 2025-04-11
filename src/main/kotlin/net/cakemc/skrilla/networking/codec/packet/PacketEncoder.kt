package net.cakemc.skrilla.networking.codec.packet

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketRegistry

class PacketEncoder(
    val registry: PacketRegistry
): MessageToByteEncoder<Packet>() {

    override fun encode(p0: ChannelHandlerContext, packet: Packet, input: ByteBuf) {
        input.writeInt(registry.packetIdByClass(packet.javaClass))
        input.writeBytes(registry.serializer.serialize(packet))
    }

}