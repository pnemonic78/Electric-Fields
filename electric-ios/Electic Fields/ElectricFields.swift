/*
 * Copyright 2018, Moshe Waisberg
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

/**
 * Electric fields view for drawing.
 *
 * @author Moshe Waisberg
 */
protocol ElectricFields {
    
    func findCharge(x: Int, y: Int) -> Charge?
    
    func invertCharge(x: Int, y: Int) -> Bool
    
    func addCharge(charge: Charge) -> Bool
    
    func addCharge(x: Int, y: Int, size: Double) -> Bool
    
    /**
     * Clear the charges.
     */
    func clear()
    
    /**
     * Start the task.
     * @param delay the start delay, in milliseconds.
     */
    func start(delay: UInt64)
    
    /**
     * Stop the task.
     */
    func stop()
    
}

extension ElectricFields {
    /** Start immediatly. */
    func start() {
        start(delay: 0)
    }
    
    /**
     * Restart the task with modified charges.
     *
     * @param delay the start delay, in milliseconds.
     */
    func restart(delay: UInt64 = 0) {
        stop()
        start(delay: delay)
    }
}
