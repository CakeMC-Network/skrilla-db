package net.cakemc.skrilla.networking.packet

import net.cakemc.skrilla.networking.packet.packets.auth.AuthRequestPacket
import net.cakemc.skrilla.networking.packet.packets.auth.AuthResponsePacket
import net.cakemc.skrilla.networking.packet.packets.system.request.*
import net.cakemc.skrilla.networking.packet.packets.system.response.*
import net.cakemc.skrilla.serial.SerializationSystem
import java.util.concurrent.ConcurrentHashMap

class PacketRegistry {

    val serializer = SerializationSystem()
    val packetMap: MutableMap<Int, Class<out Packet>> = ConcurrentHashMap()

    init {
        // AUTH
        // request
        registerPacketById(PacketIdentity.AUTH_REQUEST, AuthRequestPacket::class.java)
        // response
        registerPacketById(PacketIdentity.AUTH_RESPONSE, AuthResponsePacket::class.java)

        // DATABASE
        // request
        registerPacketById(PacketIdentity.CREATE_COLLECTION, CreateCollectionPacket::class.java)
        registerPacketById(PacketIdentity.DELETE_DOCUMENT, DeleteDocumentPacket::class.java)
        registerPacketById(PacketIdentity.FIND_DOCUMENT, FindDocumentPacket::class.java)
        registerPacketById(PacketIdentity.GET_COLLECTIONS, GetCollectionsPacket::class.java)
        registerPacketById(PacketIdentity.GET_DOCUMENT_SIZE, GetDocumentSizePacket::class.java)
        registerPacketById(PacketIdentity.INSERT_DOCUMENT, InsertDocumentPacket::class.java)
        registerPacketById(PacketIdentity.REPLACE_DOCUMENT, ReplaceDocumentPacket::class.java)
        registerPacketById(PacketIdentity.UPDATE_DOCUMENT, UpdateDocumentPacket::class.java)
        // response
        registerPacketById(PacketIdentity.COLLECTIONS_REPL, CollectionsReplyPacket::class.java)
        registerPacketById(PacketIdentity.CREATE_COLLECTION_STATUS, CreateCollectionStatusPacket::class.java)
        registerPacketById(PacketIdentity.DELETE_DOCUMENT_STATUS, DeleteDocumentStatusPacket::class.java)
        registerPacketById(PacketIdentity.DOCUMENT_REPLY, DocumentReplyPacket::class.java)
        registerPacketById(PacketIdentity.DOCUMENT_SIZE_REPL, DocumentSizeReplyPacket::class.java)
        registerPacketById(PacketIdentity.INSERT_DOCUMENT_STATUS, InsertDocumentStatusPacket::class.java)
        registerPacketById(PacketIdentity.REPLACE_DOCUMENT_STATUS, ReplaceDocumentStatusPacket::class.java)
        registerPacketById(PacketIdentity.UPDATE_DOCUMENT_STATUS, UpdateDocumentStatusPacket::class.java)
    }

    fun registerPacketById(identity: PacketIdentity, packetClass: Class<out Packet>) {
        packetMap.put(identity.ordinal, packetClass)
    }

    fun packetIdByClass(clazz: Class<out Packet>): Int {
        var foundPacketId = 0
        packetMap.forEach { packetId, packetClazz ->
            if (packetClazz.name.equals(clazz.name)) {
                foundPacketId = packetId
                return@forEach
            }
        }
        return foundPacketId
    }

    fun createPacketOutOfId(id: Int): Class<out Packet>? {
        return packetMap.getOrDefault(id, null)
    }

}