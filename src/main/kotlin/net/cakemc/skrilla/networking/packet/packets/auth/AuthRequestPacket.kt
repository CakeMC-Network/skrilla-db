package net.cakemc.skrilla.networking.packet.packets.auth

import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketType

class AuthRequestPacket(
    var user: String,
    var password: String
): Packet(
    packetType = PacketType.REQUEST.ordinal
)