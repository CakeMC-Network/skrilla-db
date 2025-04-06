package net.cakemc.skrilla.networking.packet

import net.cakemc.skrilla.networking.packet.packets.auth.AuthRequestPacket
import net.cakemc.skrilla.networking.packet.packets.auth.AuthResponsePacket
import net.cakemc.skrilla.networking.packet.packets.system.request.*
import net.cakemc.skrilla.networking.packet.packets.system.response.*
import java.util.concurrent.ConcurrentHashMap

class PacketRegistry {

    val packetMap: MutableMap<PacketIdentity, Class<out Packet>> = ConcurrentHashMap()

    init {

        // AUTH
        // request
        registerPacket(AuthRequestPacket())
        // response
        registerPacket(AuthResponsePacket())

        // DATABASE
        // request
        registerPacket(CreateCollectionPacket())
        registerPacket(DeleteDocumentPacket())
        registerPacket(FindDocumentPacket())
        registerPacket(GetCollectionsPacket())
        registerPacket(GetDocumentSizePacket())
        registerPacket(InsertDocumentPacket())
        registerPacket(ReplaceDocumentPacket())
        registerPacket(UpdateDocumentPacket())
        // response
        registerPacket(CollectionsReplyPacket())
        registerPacket(CreateCollectionPacket())
        registerPacket(DeleteDocumentStatusPacket())
        registerPacket(DocumentReplyPacket())
        registerPacket(DocumentSizeReplyPacket())
        registerPacket(InsertDocumentStatusPacket())
        registerPacket(ReplaceDocumentStatusPacket())
        registerPacket(UpdateDocumentStatusPacket())
    }

    fun registerPacket(packet: Packet) {
        this.registerPacketById(packet.packetId(), packet.javaClass)
    }

    fun registerPacketById(identity: PacketIdentity, packetClass: Class<out Packet>) {
        packetMap.put(identity, packetClass)
    }

    fun createPacketOutOfId(id: Int): Packet? {
        val identity = PacketIdentity.entries.toTypedArray()[id]
        if (packetMap.containsKey(identity))
            return null

        return packetMap.get(identity)!!.getConstructor().newInstance()
    }

}