package net.cakemc.skrilla.networking.packet.packets.system.request

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class GetCollectionsPacket: Packet(
    packetType = PacketType.REQUEST
) {

    override fun readPacket(input: ByteBuf) {}

    override fun writePacket(output: ByteBuf) {}

    override fun packetId(): PacketIdentity {
        return PacketIdentity.GET_COLLECTIONS
    }
}