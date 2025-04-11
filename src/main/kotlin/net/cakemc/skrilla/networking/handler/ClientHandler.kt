package net.cakemc.skrilla.networking.handler

import io.netty.channel.Channel
import net.cakemc.skrilla.networking.packet.*
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ClientHandler(
    val registry: PacketRegistry
) {

    val contextMap: MutableMap<String, Channel> = ConcurrentHashMap()
    val pendingPackets: MutableMap<UUID, PacketFuture> = ConcurrentHashMap()

    val handlerMap: MutableMap<Int, MutableList<PacketHandler>> = ConcurrentHashMap()

    fun registerPacketHandler(identity: PacketIdentity, handler: PacketHandler) {
        if (handlerMap.containsKey(identity.ordinal)) {
            handlerMap.get(identity.ordinal)!!.add(handler)
            return
        }
        val list: MutableList<PacketHandler> = LinkedList()
        list.add(handler)
        handlerMap.put(identity.ordinal, list)
    }

    fun packetReceived(channel: Channel, packet: Packet) {
        val handlerList = this.handlerMap.get(registry.packetIdByClass(packet.javaClass))
        if (handlerList != null) {

            handlerList.forEach { it.packetReceived(this, channel, packet) }
        }
    }

    fun getChannel(name: String): Channel? {
        return contextMap.get(name)
    }

    fun isChannelRegistered(name: String): Boolean {
        return contextMap.containsKey(name)
    }

    fun getChannels(): Set<String> {
        return contextMap.keys
    }

    fun getChannelList(): MutableCollection<Channel> {
        return contextMap.values
    }

    fun registerChannel(name: String, context: Channel) {
        this.contextMap.put(name, context)
    }

    fun unregisterChannel(name: String) {
        this.contextMap.remove(name)
    }

    fun clearChannels() {
        this.contextMap.clear()
    }

    fun getChannelNameByContext(ctx: Channel): String? {
        val entry = this.contextMap.entries.stream()
            .filter {it.value.equals(ctx)}.findFirst().orElse(null)

        if (entry == null) {
            return null
        }
        return entry.key
    }

    fun closeChannel(name: String) {
        if (isChannelRegistered(name))
            getChannel(name)!!.close()
    }

    // sending methods

    fun sendPacketSync(name: String, packet: Packet) {
        if (this.contextMap.containsKey(name))
            this.contextMap.get(name)!!.writeAndFlush(packet)
    }

    fun sendPacketWithFuture(name: String, packet: Packet): PacketFuture {
        if (this.contextMap.containsKey(name))
            this.contextMap.get(name)!!.writeAndFlush(packet)

        val future = PacketFuture()
        this.pendingPackets.put(packet.responseUUID, future)

        return future
    }

    fun replyToPacketSync(channel: Channel, received: Packet, reply: Packet) {
        val replyId = received.responseUUID

        reply.packetType = PacketType.RESPONSE.ordinal
        reply.responseUUID = replyId

        channel.writeAndFlush(reply)
    }

    fun sendToAllSync(packet: Packet) {
        this.contextMap.values.forEach { it.writeAndFlush(packet) }
    }

    fun sendPacketAsync(name: String, packet: Packet) {
        if (this.contextMap.containsKey(name)) {
            val context = this.contextMap[name]
            if (context != null) {
                Thread.ofVirtual().start {
                    context.writeAndFlush(packet)
                }
            }
        }
    }

    fun sendPacketWithFutureAsync(name: String, packet: Packet): PacketFuture {
        val future = PacketFuture()
        // Add the future to pending packets
        this.pendingPackets.put(packet.responseUUID, future)

        if (this.contextMap.containsKey(name)) {
            val context = this.contextMap[name]
            if (context != null) {
                Thread.ofVirtual().start {
                    context.writeAndFlush(packet)
                }
            }
        }

        return future
    }

    fun sendToAllAsync(packet: Packet) {
        this.contextMap.values.forEach { context ->
            Thread.ofVirtual().start {
                context.writeAndFlush(packet)
            }
        }
    }

    fun replyToPacketAsync(channel: Channel, received: Packet, reply: Packet) {
        val replyId = received.responseUUID
        reply.packetType = PacketType.RESPONSE.ordinal
        reply.responseUUID = replyId

        Thread.ofVirtual().start {
            channel.writeAndFlush(reply)
        }

    }

}