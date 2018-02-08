package com.github.fields.electric

/**
 * Electric fields view for drawing.
 *
 * @author Moshe Waisberg
 */
interface ElectricFields {

    fun findCharge(x: Int, y: Int): Charge?

    fun invertCharge(x: Int, y: Int): Boolean

    fun addCharge(charge: Charge): Boolean

    fun addCharge(x: Int, y: Int, size: Double): Boolean

    /**
     * Clear the charges.
     */
    fun clear()

    /**
     * Start the task.
     * @param delay the start delay, in milliseconds.
     */
    fun start(delay: Long = 0L)

    /**
     * Stop the task.
     */
    fun stop()

    /**
     * Restart the task with modified charges.
     *
     * @param delay the start delay, in milliseconds.
     */
    fun restart(delay: Long = 0L) {
        stop()
        start(delay)
    }
}