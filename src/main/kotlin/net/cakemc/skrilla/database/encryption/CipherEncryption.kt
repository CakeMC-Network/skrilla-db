package net.cakemc.database.encryption

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*

/**
 * The type Cipher encryption.
 */
class CipherEncryption(keyFile: AbstractKey) : FileEncryption() {
    private var decrypt: Cipher
    private var encrypt: Cipher

    /**
     * Instantiates a new Cipher encryption.
     *
     * @param keyFile the key file
     */
    init {
        val secretKey = keyFile.key

        try {
            encrypt = Cipher.getInstance(keyFile.algorithm)
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException(e)
        }

        try {
            decrypt = Cipher.getInstance(keyFile.algorithm)
            decrypt.init(Cipher.DECRYPT_MODE, secretKey)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException(e)
        }
    }

    override fun encrypt(source: ByteArray): ByteArray {
        if (source.size == 0) return source

        try {
            return encrypt!!.doFinal(source)
        } catch (e: IllegalBlockSizeException) {
            throw RuntimeException(e)
        } catch (e: BadPaddingException) {
            throw RuntimeException(e)
        }
    }

    override fun decrypt(source: ByteArray): ByteArray {
        if (source.size == 0) return source

        try {
            return decrypt!!.doFinal(source)
        } catch (e: IllegalBlockSizeException) {
            throw RuntimeException(e)
        } catch (e: BadPaddingException) {
            throw RuntimeException(e)
        }
    }
}