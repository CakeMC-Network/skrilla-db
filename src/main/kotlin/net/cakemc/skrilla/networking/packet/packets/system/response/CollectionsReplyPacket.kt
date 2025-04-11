package net.cakemc.skrilla.networking.packet.packets.system.response

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class CollectionsReplyPacket(
    var names: Array<String>
): Packet(
    packetType = PacketType.RESPONSE.ordinal
)