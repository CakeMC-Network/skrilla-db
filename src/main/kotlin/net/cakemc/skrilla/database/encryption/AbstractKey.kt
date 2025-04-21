package net.cakemc.database.encryption

import javax.crypto.SecretKey

/**
 * Abstract base class for encryption keys, providing a structure for subclasses
 * to define specific types of encryption keys. This class encapsulates a secret key
 * and its associated encryption algorithm.
 *
 * Subclasses must provide the actual key and algorithm used for encryption or decryption.
 */
abstract class AbstractKey {

    /**
     * Retrieves the encryption key.
     *
     * @return The [SecretKey] instance representing the encryption key.
     *         It may return null if the key is not available.
     */
    abstract val key: SecretKey?

    /**
     * Retrieves the name of the encryption algorithm used with this key.
     *
     * @return The algorithm name as a [String]. It may return null if the
     *         algorithm is not specified.
     */
    abstract val algorithm: String?
}
