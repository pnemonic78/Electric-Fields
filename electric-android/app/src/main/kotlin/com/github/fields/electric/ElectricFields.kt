/*
 * Copyright 2016, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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