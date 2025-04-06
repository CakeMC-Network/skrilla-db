package net.cakemc.skrilla.networking.packet.packets.auth

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class AuthRequestPacket(
    var user: String,
    var password: String
): Packet(
    packetType = PacketType.REQUEST
) {

    constructor(): this("", "")

    override fun readPacket(input: ByteBuf) {
        user = readString(input)
        password = readString(input)
    }

    override fun writePacket(output: ByteBuf) {
        writeString(output, user)
        writeString(output, password)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.AUTH_REQUEST
    }
}