package net.cakemc.skrilla.natives.file

/**
 * Factory for creating platform-specific FileUtility implementations.
 */
object FileUtilityFactory {
    fun create(): FileUtility {
        return if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            WindowsFileUtility()
        } else {
            net.cakemc.skrilla.natives.file.LinuxFileUtility()
        }
    }
}
