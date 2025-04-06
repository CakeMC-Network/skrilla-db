package net.cakemc.database.units

/**
 * The interface Key supplier.
 *
 * @param <T> the type parameter
 * @param <B> the type parameter
</B></T> */
fun interface KeySupplier<T, B> {
    /**
     * Accept t.
     *
     * @param key the key
     * @return the t
     */
    fun accept(key: B): T
}

