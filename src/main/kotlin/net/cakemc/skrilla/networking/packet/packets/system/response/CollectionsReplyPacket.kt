package net.cakemc.skrilla.networking.packet.packets.system.response

import io.netty.buffer.ByteBuf
import net.cakemc.skrilla.networking.packet.Packet
import net.cakemc.skrilla.networking.packet.PacketIdentity
import net.cakemc.skrilla.networking.packet.PacketType

class CollectionsReplyPacket(var names: MutableList<String>): Packet(
    packetType = PacketType.RESPONSE
) {

    constructor() : this(ArrayList())

    override fun readPacket(input: ByteBuf) {
        names = ArrayList()
        val size = input.readInt()
        for (i in 0 until size) {
            names.add(readString(input))
        }
    }

    override fun writePacket(output: ByteBuf) {
        output.writeInt(names.size)
        names.forEach {
            writeString(output, it)
        }
    }

    override fun packetId(): PacketIdentity {
        return PacketIdentity.COLLECTIONS_REPL
    }


}