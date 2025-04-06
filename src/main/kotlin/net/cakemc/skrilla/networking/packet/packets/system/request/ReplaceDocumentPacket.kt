package net.cakemc.skrilla.networking.packet.packets.system.request

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class ReplaceDocumentPacket(var collection: String, var filter: String, var document: String): Packet(
    packetType = PacketType.REQUEST
) {

    constructor() : this("", "", "")

    override fun readPacket(input: ByteBuf) {
        collection = readString(input)
        filter = readString(input)
        document = readString(input)
    }

    override fun writePacket(output: ByteBuf) {
        writeString(output, collection)
        writeString(output, filter)
        writeString(output, document)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.REPLACE_DOCUMENT
    }

}