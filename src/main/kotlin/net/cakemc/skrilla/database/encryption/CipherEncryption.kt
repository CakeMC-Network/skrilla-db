package net.cakemc.database.encryption

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*


/**
 * A concrete implementation of [FileEncryption] that uses the Java Cryptography Architecture (JCA)
 * with the [Cipher] class for encryption and decryption operations.
 *
 * This class requires an instance of [AbstractKey] which holds the secret key for encryption/decryption
 * and the algorithm to be used. It provides methods to encrypt and decrypt byte arrays using
 * the specified cipher algorithm.
 *
 * @param keyFile The key file containing the secret key and algorithm for encryption and decryption.
 *
 * @throws RuntimeException if there are issues with key initialization or cipher operations.
 */
class CipherEncryption(keyFile: AbstractKey) : FileEncryption() {
    private var decrypt: Cipher
    private var encrypt: Cipher

    /**
     * Initializes the cipher encryption with the given [AbstractKey].
     * It sets up both encryption and decryption ciphers based on the key's algorithm.
     *
     * @param keyFile The key file containing the secret key and algorithm.
     * @throws RuntimeException if the algorithm or padding is not available, or if the key is invalid.
     */
    init {
        val secretKey = keyFile.key

        try {
            // Initialize the encryption cipher
            encrypt = Cipher.getInstance(keyFile.algorithm)
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Algorithm not found", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("No padding scheme available", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Invalid key provided", e)
        }

        try {
            // Initialize the decryption cipher
            decrypt = Cipher.getInstance(keyFile.algorithm)
            decrypt.init(Cipher.DECRYPT_MODE, secretKey)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Algorithm not found", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("No padding scheme available", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Invalid key provided", e)
        }
    }

    /**
     * Encrypts the provided byte array using the initialized encryption cipher.
     *
     * @param source The byte array to be encrypted.
     * @return A new byte array containing the encrypted data.
     * @throws RuntimeException if encryption fails due to illegal block size or padding issues.
     */
    override fun encrypt(source: ByteArray): ByteArray {
        if (source.isEmpty()) return source

        try {
            return encrypt.doFinal(source)
        } catch (e: IllegalBlockSizeException) {
            throw RuntimeException("Encryption error: Illegal block size", e)
        } catch (e: BadPaddingException) {
            throw RuntimeException("Encryption error: Bad padding", e)
        }
    }

    /**
     * Decrypts the provided byte array using the initialized decryption cipher.
     *
     * @param source The byte array to be decrypted.
     * @return A new byte array containing the decrypted data.
     * @throws RuntimeException if decryption fails due to illegal block size or padding issues.
     */
    override fun decrypt(source: ByteArray): ByteArray {
        if (source.isEmpty()) return source

        try {
            return decrypt.doFinal(source)
        } catch (e: IllegalBlockSizeException) {
            throw RuntimeException("Decryption error: Illegal block size", e)
        } catch (e: BadPaddingException) {
            throw RuntimeException("Decryption error: Bad padding", e)
        }
    }
}
