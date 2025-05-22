package net.cakemc.skrilla.datatrack

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DataTracker(
    val documentName: String = ""
)
