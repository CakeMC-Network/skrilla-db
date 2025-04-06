package recode

import java.util.concurrent.ThreadLocalRandom
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object TestKeyGen {
    fun generateRSAKey(): SecretKey {
        val keyComp = fillArray(ByteArray(256), 256)
        return SecretKeySpec(keyComp, "RSA")
    }

    fun generateAESKey(): SecretKey {
        val keyComp = fillArray(ByteArray(16), 16)
        return SecretKeySpec(keyComp, "AES")
    }


    @JvmStatic
    fun main(args: Array<String>) {
        println("AES " + generateAESKey().encoded.contentToString())
        println("RSA " + generateRSAKey().encoded.contentToString())
    }

    fun fillArray(toFill: ByteArray, length: Int): ByteArray {
        for (i in 0 until length) toFill[i] = ThreadLocalRandom.current().nextInt().toByte()
        return toFill
    }
}
