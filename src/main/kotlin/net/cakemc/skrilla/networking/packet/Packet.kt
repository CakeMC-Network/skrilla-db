package net.cakemc.skrilla.networking.packet

import java.util.*

abstract class Packet(
    var responseUUID: UUID = UUID.randomUUID(),
    var packetType: Int
)