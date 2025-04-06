package net.cakemc.skrilla.networking.packet

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset
import java.util.*

abstract class Packet(
    var responseUUID: UUID = UUID.randomUUID(),
    var packetType: PacketType
) {

    fun read(input: ByteBuf) {
        val most = readLong(input)
        val least = readLong(input)

        this.packetType = PacketType.entries.toTypedArray()[input.readInt()]
        this.responseUUID = UUID(most, least)

        readPacket(input)
    }

    abstract fun readPacket(input: ByteBuf)

    fun write(output: ByteBuf) {
        output.writeInt(this.packetId().ordinal)

        writeLongs(responseUUID.mostSignificantBits, output)
        writeLongs(responseUUID.leastSignificantBits, output)
        output.writeInt(packetType.ordinal)

        writePacket(output)
    }

    abstract fun writePacket(output: ByteBuf)

    abstract fun packetId(): PacketIdentity

    fun readString(input: ByteBuf): String {
        val length = input.readInt()
        val bytes = ByteArray(length)
        input.readBytes(bytes)
        return String(bytes)
    }

    fun writeString(output: ByteBuf, text: String) {
        output.writeInt(text.length)
        output.writeBytes(text.toByteArray(Charset.defaultCharset()))
    }

    protected fun writeLongs(value: Long, byteBuf: ByteBuf): ByteBuf {
        var value = value
        do {
            var temp = (value and 127L).toByte()
            value = value ushr 7
            if (value != 0L) temp = (temp.toInt() or 128).toByte()

            byteBuf.writeByte(temp.toInt())
        } while (value != 0L)

        return byteBuf
    }

    protected fun readLong(byteBuf: ByteBuf): Long {
        var numRead = 0
        var result: Long = 0
        var read: Byte
        do {
            read = byteBuf.readByte()
            val value = (read.toInt() and 127)
            result = result or (value shl (7 * numRead)).toLong()

            numRead++
        } while ((read.toInt() and 128) != 0)

        return result
    }


}