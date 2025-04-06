package net.cakemc.skrilla.networking.codec.compress

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import net.cakemc.database.AbstractDatabase

class CompressionEncoder: MessageToMessageEncoder<ByteBuf>() {

    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val byteArray = ByteArray(msg.readableBytes())
        msg.readBytes(byteArray)

        // Compress the byte array
        val compressed = AbstractDatabase.DEFAULT_COMPRESSION.compress(byteArray)

        // Convert the compressed byte array into a ByteBuf
        val compressedBuf = ctx.alloc().buffer(compressed.size)
        compressedBuf.writeBytes(compressed)

        // Add the compressed ByteBuf to the output list
        out.add(compressedBuf)
    }

}
