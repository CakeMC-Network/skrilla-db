package net.cakemc.skrilla.database.imdb

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A thread-safe synchronization primitive for tracking the completion of a set of operations.
 * It allows for adding tasks, marking tasks as done, and waiting until all tasks are complete.
 */
class WaitGroup {

    /**
     * The count of tasks that need to be completed.
     */
    private var count = 0

    /**
     * The lock used to synchronize access to the count.
     */
    private val lock = ReentrantLock()

    /**
     * The condition variable used for waiting until the count reaches zero.
     */
    private val condition = lock.newCondition()

    /**
     * Increments the count by the specified delta.
     *
     * @param delta The number to increment the count by. Can be negative for decrementing.
     */
    fun add(delta: Int) {
        lock.withLock {
            count += delta
            if (count <= 0) {
                condition.signalAll()
            }
        }
    }

    /**
     * Decrements the count by one, indicating that a task is completed.
     */
    fun done() {
        lock.withLock {
            count--
            if (count <= 0) {
                condition.signalAll()
            }
        }
    }

    /**
     * Blocks the current thread until the count reaches zero.
     * This method will release the lock while waiting.
     */
    fun waitEmpty() {
        lock.withLock {
            while (count > 0) {
                try {
                    condition.await()
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt() // Restore the interrupt flag
                    throw e
                }
            }
        }
    }
}
