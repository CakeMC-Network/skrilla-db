package net.cakemc.skrilla.networking.packet.packets.auth

import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketType

class AuthResponsePacket(
    var authStatus: Int
): Packet(
    packetType = PacketType.RESPONSE.ordinal
)