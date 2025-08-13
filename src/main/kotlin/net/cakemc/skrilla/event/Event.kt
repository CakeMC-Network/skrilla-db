package net.cakemc.skrilla.event

abstract class Event : Cancellable() {

    override var cancelState: Boolean = false

}