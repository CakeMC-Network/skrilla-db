package net.cakemc.skrilla.networking.packet

import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PacketFuture : Future<Packet> {

    private enum class PacketFutureState {
        WAITING,
        CANCELLED,
        FAILED,
        DONE
    }

    private var value: Packet? = null

    @Volatile
    private var state: PacketFutureState = PacketFutureState.WAITING
    private val waitingObject = Object()

    /**
     * Attempts to cancel execution of this task.  This method has no
     * effect if the task is already completed or cancelled, or could
     * not be cancelled for some other reason.  Otherwise, if this
     * task has not started when `cancel` is called, this task
     * should never run.  If the task has already started, then the
     * `mayInterruptIfRunning` parameter determines whether the
     * thread executing this task (when known by the implementation)
     * is interrupted in an attempt to stop the task.
     *
     *
     * The return value from this method does not necessarily
     * indicate whether the task is now cancelled; use [ ][.isCancelled].
     *
     * @param mayInterruptIfRunning `true` if the thread
     * executing this task should be interrupted (if the thread is
     * known to the implementation); otherwise, in-progress tasks are
     * allowed to complete
     * @return `false` if the task could not be cancelled,
     * typically because it has already completed; `true`
     * otherwise. If two or more threads cause a task to be cancelled,
     * then at least one of them returns `true`. Implementations
     * may provide stronger guarantees.
     */
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        synchronized(waitingObject) {
            if (isDone)
                return false

            this.state = PacketFutureState.CANCELLED
            waitingObject.notifyAll()
        }
        return true
    }

    /**
     * Returns `true` if this task was cancelled before it completed
     * normally.
     *
     * @return `true` if this task was cancelled before it completed
     */
    override fun isCancelled(): Boolean {
        return state.equals(PacketFutureState.CANCELLED)
    }

    /**
     * Returns `true` if this task completed.
     *
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * `true`.
     *
     * @return `true` if this task completed
     */
    override fun isDone(): Boolean {
        return !state.equals(PacketFutureState.WAITING)
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     */
    override fun get(): Packet {
        synchronized(waitingObject) {
            await()
            hasFailed()
        }

        return value!!
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     * @throws TimeoutException if the wait timed out
     */
    override fun get(timeout: Long, unit: TimeUnit): Packet {
        synchronized(waitingObject) {
            await(timeout, unit)
            hasFailed()

            return value!!
        }
    }

    fun set(packet: Packet): Boolean {
        synchronized(waitingObject) {
            if (isDone)
                return false

            this.state = PacketFutureState.DONE
            this.value = packet
            waitingObject.notifyAll()
        }
        return true
    }

    private fun await(timeout: Long, unit: TimeUnit) {
        synchronized(waitingObject) {
            val end = System.currentTimeMillis() + unit.toMillis(timeout)
            do {
                try {
                    waitingObject.wait(end - System.currentTimeMillis())
                } catch (exception: InterruptedException) {
                    exception.printStackTrace()
                }
            } while (state.equals(PacketFutureState.WAITING) && System.currentTimeMillis() < end)

            if (state.equals(PacketFutureState.WAITING))
                throw TimeoutException("packet future timeout!")
        }
    }

    private fun await() {
        synchronized(waitingObject) {

            do {
                try {
                    waitingObject.wait()
                } catch (exception: InterruptedException) {
                    exception.printStackTrace()
                }
            } while (state.equals(PacketFutureState.WAITING))

        }
    }

    private fun hasFailed() {
        if (state.equals(PacketFutureState.FAILED) || state.equals(PacketFutureState.CANCELLED))
            throw IllegalStateException()
    }

    fun syncUninterruptedly(): Packet {
        synchronized(waitingObject) {
            return get()
        }
    }

    fun syncUninterruptedly(timeout: Long, unit: TimeUnit): Packet {
        synchronized(waitingObject) {
            return get(timeout, unit)
        }
    }



}