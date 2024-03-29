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

import android.graphics.Bitmap

/**
 * Electric fields event listener.
 *
 * @author Moshe Waisberg
 */
interface ElectricFieldsListener {

    fun onChargeAdded(view: ElectricFields, charge: Charge)

    fun onChargeInverted(view: ElectricFields, charge: Charge)

    fun onChargeScaleBegin(view: ElectricFields, charge: Charge): Boolean

    fun onChargeScale(view: ElectricFields, charge: Charge): Boolean

    fun onChargeScaleEnd(view: ElectricFields, charge: Charge): Boolean

    fun onRenderFieldClicked(view: ElectricFields, x: Int, y: Int, size: Double): Boolean

    fun onRenderFieldStarted(view: ElectricFields)

    fun onRenderFieldProgress(view: ElectricFields, field: Bitmap)

    fun onRenderFieldFinished(view: ElectricFields)

    fun onRenderFieldCancelled(view: ElectricFields)
}
