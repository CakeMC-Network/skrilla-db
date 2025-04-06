package net.cakemc.database.encryption

import javax.crypto.SecretKey

/**
 * The type Abstract key.
 */
abstract class AbstractKey {
    /**
     * Gets key.
     *
     * @return the key
     */
    abstract val key: SecretKey?

    /**
     * Gets algorithm.
     *
     * @return the algorithm
     */
    abstract val algorithm: String?
}
