package net.cakemc.database.encryption

/**
 * The type File encryption.
 */
abstract class FileEncryption {
    /**
     * Encrypt byte [ ].
     *
     * @param source the source
     * @return the byte [ ]
     */
    abstract fun encrypt(source: ByteArray): ByteArray

    /**
     * Decrypt byte [ ].
     *
     * @param source the source
     * @return the byte [ ]
     */
    abstract fun decrypt(source: ByteArray): ByteArray
}
