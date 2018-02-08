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

import Foundation
import CoreGraphics

/**
 * Electric charge particle.
 *
 * @author Moshe Waisberg
 */
struct Charge {

    var x: Int = 0
    var y: Int = 0
    var size: Double = 0.0
    
    /**
     * Set the point's x and y coordinates, and size.
     */
    mutating func set(x: Int, y: Int, size: Double) {
        self.x = x
        self.y = y
        self.size = size
    }
    
    public var description: String {
        return "Charge(\(x), \(y), \(size))"
    }
}
