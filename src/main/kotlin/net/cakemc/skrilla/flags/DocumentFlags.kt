package net.cakemc.skrilla.flags

enum class DocumentFlags(val bit: Int, val serializer: FlagSerializer<*>? = null) {
    AUTO_DELETE(1 shl 0, InstantSerializer),
    FORCE_WRITE(1 shl 1),

    ;

    companion object {
        fun fromInt(flags: Int): Set<DocumentFlags> =
            values().filter { flags and it.bit != 0 }.toSet()

        fun toInt(vararg flags: DocumentFlags): Int =
            flags.fold(0) { acc, flag -> acc or flag.bit }

        fun toInt(flags: Collection<DocumentFlags>): Int =
            flags.fold(0) { acc, flag -> acc or flag.bit }
    }
}