package net.cakemc.skrilla.networking.codec.crypt

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.ShortBufferException
import javax.crypto.spec.IvParameterSpec


class DefaultCipher: AbstractCipher() {

    private var cipher: Cipher? = null
    private
    val heapInLocal: ThreadLocal<ByteArray> = EmptyByteThreadLocal()
    private
    val heapOutLocal: ThreadLocal<ByteArray> = EmptyByteThreadLocal()

    private
    class EmptyByteThreadLocal : ThreadLocal<ByteArray>() {
        override fun initialValue(): ByteArray {
            return ByteArray(0)
        }
    }

    init {
        try {
            this.cipher = Cipher.getInstance("AES/CFB8/NoPadding")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }
    }

    @Throws(GeneralSecurityException::class)
    override fun init(forEncryption: Boolean, key: SecretKey) {
        val mode = if (forEncryption) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE
        cipher!!.init(mode, key, IvParameterSpec(key.encoded))
    }

    @Throws(ShortBufferException::class)
    override fun cipher(`in`: ByteBuf, out: ByteBuf) {
        val readableBytes = `in`.readableBytes()
        val heapIn = bufToByte(`in`)

        var heapOut = heapOutLocal.get()
        val outputSize = cipher!!.getOutputSize(readableBytes)
        if (heapOut.size < outputSize) {
            heapOut = ByteArray(outputSize)
            heapOutLocal.set(heapOut)
        }
        out.writeBytes(heapOut, 0, cipher!!.update(heapIn, 0, readableBytes, heapOut))
    }

    @Throws(ShortBufferException::class)
    override fun cipher(ctx: ChannelHandlerContext, `in`: ByteBuf): ByteBuf {
        val readableBytes = `in`.readableBytes()
        val heapIn = bufToByte(`in`)

        val heapOut = ctx.alloc().heapBuffer(cipher!!.getOutputSize(readableBytes))
        heapOut.writerIndex(cipher!!.update(heapIn, 0, readableBytes, heapOut.array(), heapOut.arrayOffset()))

        return heapOut
    }

    override fun free() {
    }

    private fun bufToByte(`in`: ByteBuf): ByteArray {
        var heapIn = heapInLocal.get()
        val readableBytes = `in`.readableBytes()
        if (heapIn.size < readableBytes) {
            heapIn = ByteArray(readableBytes)
            heapInLocal.set(heapIn)
        }
        `in`.readBytes(heapIn, 0, readableBytes)
        return heapIn
    }
}