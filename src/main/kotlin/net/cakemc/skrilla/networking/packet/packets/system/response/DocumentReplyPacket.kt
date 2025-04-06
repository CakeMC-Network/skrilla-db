package net.cakemc.skrilla.networking.packet.packets.system.response

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class DocumentReplyPacket(var document: String): Packet(
    packetType = PacketType.RESPONSE
) {

    constructor() : this("")

    override fun readPacket(input: ByteBuf) {
        document = readString(input)
    }

    override fun writePacket(output: ByteBuf) {
        writeString(output, document)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.DOCUMENT_REPLY
    }
}