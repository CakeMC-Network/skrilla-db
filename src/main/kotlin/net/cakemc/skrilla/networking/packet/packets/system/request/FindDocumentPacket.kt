package net.cakemc.skrilla.networking.packet.packets.system.request

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class FindDocumentPacket(var collection: String, var filter: String): Packet(
    packetType = PacketType.REQUEST
) {

    constructor() : this("", "")

    override fun readPacket(input: ByteBuf) {
        collection = readString(input)
        filter = readString(input)
    }

    override fun writePacket(output: ByteBuf) {
        writeString(output, collection)
        writeString(output, filter)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.FIND_DOCUMENT
    }


}