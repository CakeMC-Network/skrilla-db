package net.cakemc.skrilla.networking.packet.packets.auth

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class AuthResponsePacket(
    var authStatus: AuthStatus
): Packet(
    packetType = PacketType.RESPONSE
) {

    constructor(): this(AuthStatus.FAILED)

    override fun readPacket(input: ByteBuf) {
        authStatus = AuthStatus.entries.toTypedArray()[input.readInt()]
    }

    override fun writePacket(output: ByteBuf) {
        output.writeInt(authStatus.ordinal)
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.AUTH_RESPONSE
    }

}