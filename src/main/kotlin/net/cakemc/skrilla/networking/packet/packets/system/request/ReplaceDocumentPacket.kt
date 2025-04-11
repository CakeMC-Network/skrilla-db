package net.cakemc.skrilla.networking.packet.packets.system.request

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class ReplaceDocumentPacket(
    var collection: String, var filter: String, var document: String
): Packet(
    packetType = PacketType.REQUEST.ordinal
)