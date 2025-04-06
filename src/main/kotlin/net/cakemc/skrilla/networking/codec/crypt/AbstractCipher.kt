package net.cakemc.skrilla.networking.codec.crypt

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import javax.crypto.SecretKey

abstract class AbstractCipher {

    abstract fun init(encrypt: Boolean, key: SecretKey)
    abstract fun free()
    abstract fun cipher(input: ByteBuf, output: ByteBuf)
    abstract fun cipher(ctx: ChannelHandlerContext, input: ByteBuf): ByteBuf

}