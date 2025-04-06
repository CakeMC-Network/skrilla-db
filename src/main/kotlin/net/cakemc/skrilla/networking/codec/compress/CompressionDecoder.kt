package net.cakemc.skrilla.networking.codec.compress

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import net.cakemc.database.AbstractDatabase

class CompressionDecoder: MessageToMessageDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val byteArray = ByteArray(msg.readableBytes())
        msg.readBytes(byteArray)

        // Decompress the byte array
        val decompressed = AbstractDatabase.DEFAULT_COMPRESSION.decompress(byteArray)

        // Convert the decompressed byte array into a ByteBuf
        val decompressedBuf = ctx.alloc().buffer(decompressed.size)
        decompressedBuf.writeBytes(decompressed)

        // Add the decompressed ByteBuf to the output list
        out.add(decompressedBuf)
    }

}