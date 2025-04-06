package net.cakemc.skrilla.networking.packet.packets.system.response

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class DocumentSizeReplyPacket(var size: Int): Packet(
    packetType = PacketType.RESPONSE
) {

    constructor() : this(0)

    override fun readPacket(input: ByteBuf) {
        size = input.readInt()
    }

    override fun writePacket(output: ByteBuf) {
        output.writeInt(size)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.DOCUMENT_SIZE_REPL
    }

}