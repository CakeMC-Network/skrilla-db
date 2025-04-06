package net.cakemc.skrilla.networking.packet.packets.system.request

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class DeleteDocumentPacket(var collection: String, var documentId: Long): Packet(packetType = PacketType.REQUEST) {

    constructor() : this("", 0L)

    override fun readPacket(input: ByteBuf) {
        this.collection = readString(input)
        this.documentId = readLong(input)
    }

    override fun writePacket(output: ByteBuf) {
        writeString(output, collection)
        writeLongs(documentId, output)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.DELETE_DOCUMENT
    }
}