package net.cakemc.skrilla.networking.packet.packets.system.response

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class UpdateDocumentStatusPacket(var status: Boolean): Packet(
    packetType = PacketType.RESPONSE
) {

    constructor() : this(false)

    override fun readPacket(input: ByteBuf) {
        status = input.readBoolean()
    }

    override fun writePacket(output: ByteBuf) {
        output.writeBoolean(status)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.UPDATE_DOCUMENT_STATUS
    }

}