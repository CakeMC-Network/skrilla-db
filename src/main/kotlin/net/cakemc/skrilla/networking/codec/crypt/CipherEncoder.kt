package net.cakemc.skrilla.networking.codec.crypt

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import javax.crypto.SecretKey

class CipherEncoder(
    val secretKey: SecretKey
): MessageToByteEncoder<ByteBuf>() {

    val abstractCipher: AbstractCipher

    init {
        abstractCipher = DefaultCipher()
        abstractCipher.init(true, secretKey)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        abstractCipher.free()
    }

    override fun encode(ctx: ChannelHandlerContext, input: ByteBuf, output: ByteBuf) {
        abstractCipher.cipher(input, output)
    }

}