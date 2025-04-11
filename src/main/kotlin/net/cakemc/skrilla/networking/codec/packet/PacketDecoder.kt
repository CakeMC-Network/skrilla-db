package net.cakemc.skrilla.networking.codec.packet

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketRegistry
import net.cakemc.skrilla.networking.packet.packets.auth.AuthRequestPacket

class PacketDecoder(
    val registry: PacketRegistry
): ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, output: MutableList<Any>) {
        val packetId = input.readInt()
        val packetClass = registry.createPacketOutOfId(packetId) as Class<*>

        val packetSize = input.readableBytes()
        val packetData = ByteArray(packetSize)
        input.readBytes(packetData)

        val packet: Packet? = registry.serializer.deserialize(packetData, packetClass) as Packet?
        if (packet == null)
            return

        output.add(packet)
    }

}