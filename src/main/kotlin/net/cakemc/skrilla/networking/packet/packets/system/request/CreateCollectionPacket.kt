package net.cakemc.skrilla.networking.packet.packets.system.request

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class CreateCollectionPacket(var name: String): Packet(
    packetType = PacketType.REQUEST
) {

    constructor() : this("")

    override fun readPacket(input: ByteBuf) {
        name = readString(input)
    }

    override fun writePacket(output: ByteBuf) {
        writeString(output, name)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.CREATE_COLLECTION
    }
}