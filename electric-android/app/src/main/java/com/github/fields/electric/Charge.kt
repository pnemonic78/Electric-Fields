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

import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable

/**
 * Electric charge particle.

 * @author Moshe Waisberg
 */
open class Charge(x: Int, y: Int, size: Double = 0.0) : Point(x, y) {

    var size: Double = 0.0

    init {
        this.size = size
    }

    /**
     * Set the point's x and y coordinates, and size.
     */
    operator fun set(x: Int, y: Int, size: Double) {
        set(x, y)
        this.size = size
    }

    override fun toString(): String {
        return "Charge($x, $y, $size)"
    }

    override fun readFromParcel(p: Parcel) {
        super.readFromParcel(p)
        size = p.readDouble()
    }

    companion object {

        val CREATOR: Parcelable.Creator<Charge> = object : Parcelable.Creator<Charge> {

            override fun createFromParcel(p: Parcel): Charge {
                val r = Charge(0, 0, 0.0)
                r.readFromParcel(p)
                return r
            }

            override fun newArray(size: Int): Array<Charge?> {
                return arrayOfNulls(size)
            }
        }
    }

}
