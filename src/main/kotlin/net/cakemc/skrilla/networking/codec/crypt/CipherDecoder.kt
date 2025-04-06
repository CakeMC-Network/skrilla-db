package net.cakemc.skrilla.networking.codec.crypt

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import javax.crypto.SecretKey

class CipherDecoder(
    val secretKey: SecretKey
): MessageToMessageDecoder<ByteBuf>() {

    val abstractCipher: AbstractCipher

    init {
        abstractCipher = DefaultCipher()
        abstractCipher.init(false, secretKey)
    }

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, output: MutableList<Any>) {
        output.add(abstractCipher.cipher(ctx, input))
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        abstractCipher.free()
    }

}