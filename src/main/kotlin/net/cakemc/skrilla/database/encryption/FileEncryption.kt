package net.cakemc.database.encryption

/**
 * Abstract class that defines the structure for file encryption algorithms.
 * This class is intended to be subclassed by concrete encryption algorithms
 * that provide the specific methods for encrypting and decrypting byte arrays.
 */
abstract class FileEncryption {

    /**
     * Encrypts the provided byte array using a specific encryption algorithm.
     *
     * @param source The byte array to be encrypted.
     * @return A new byte array containing the encrypted data.
     * @throws EncryptionException if an error occurs during encryption.
     */
    abstract fun encrypt(source: ByteArray): ByteArray

    /**
     * Decrypts the provided byte array using a specific decryption algorithm.
     *
     * @param source The byte array to be decrypted.
     * @return A new byte array containing the decrypted data.
     * @throws DecryptionException if an error occurs during decryption.
     */
    abstract fun decrypt(source: ByteArray): ByteArray
}
