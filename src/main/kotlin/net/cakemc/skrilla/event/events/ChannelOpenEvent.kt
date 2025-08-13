package net.cakemc.skrilla.event.events

import io.netty.channel.Channel
import net.cakemc.skrilla.event.Event
import net.cakemc.skrilla.networking.packet.Packet

class ChannelOpenEvent(
    val channel: Channel
): Event()

class ChannelCloseEvent(
    val channel: Channel
): Event()

class PacketIncomingEvent(
    val channel: Channel,
    val packet: Packet
): Event()