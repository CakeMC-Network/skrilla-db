package net.cakemc.skrilla.networking.packet

import java.util.concurrent.ConcurrentHashMap

class PacketRegistry {

    val packetMap: MutableMap<PacketIdentity, Class<out Packet>> = ConcurrentHashMap()

    fun registerPacket(identity: PacketIdentity, packetClass: Class<out Packet>) {
        packetMap.put(identity, packetClass)
    }

    fun createPacketOutOfId(id: Int): Packet? {
        val identity = PacketIdentity.entries.toTypedArray()[id]
        if (packetMap.containsKey(identity))
            return null

        return packetMap.get(identity)!!.getConstructor().newInstance()
    }

}